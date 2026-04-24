package com.velora.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.payment.event.PaymentEvent;
import com.velora.payment.model.Payment;
import com.velora.payment.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class PaymentService {
    private final PaymentRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.payment-success}")
    private String paymentSuccessTopic;
    @Value("${app.kafka.topics.payment-failed}")
    private String paymentFailedTopic;

    public PaymentService(PaymentRepository repository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void processOrderEvent(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        Long orderId = event.get("orderId").asLong();
        String paymentMethod = event.get("paymentMethod").asText();
        boolean failed = "FAIL".equalsIgnoreCase(paymentMethod) || "FAILED".equalsIgnoreCase(paymentMethod);

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setCorrelationId(event.get("correlationId").asText());
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(event.get("totalAmount").decimalValue());
        payment.setStatus(failed ? Payment.Status.FAILED : Payment.Status.PAID);
        repository.save(payment);

        System.out.println("""
                
                ============================================================
                PAYMENT SERVICE - PAYMENT SAVED
                ============================================================
                Payment ID     : %s
                Order ID       : %s
                Customer       : %s
                Email          : %s
                Method         : %s
                Amount         : %s
                Status         : %s
                Reason         : %s
                Correlation ID : %s
                ============================================================
                """.formatted(
                payment.getId(),
                orderId,
                event.get("username").asText(),
                event.get("email").asText(),
                paymentMethod,
                payment.getAmount(),
                payment.getStatus(),
                failed ? "Payment rejected by simulation" : "Payment completed",
                payment.getCorrelationId()
        ));

        PaymentEvent out = new PaymentEvent();
        out.eventId = UUID.randomUUID().toString();
        out.occurredAt = Instant.now();
        out.correlationId = payment.getCorrelationId();
        out.orderId = orderId;
        out.paymentId = payment.getId();
        out.amount = payment.getAmount();
        out.status = payment.getStatus().name();
        out.paymentMethod = paymentMethod;
        out.username = event.get("username").asText();
        out.email = event.get("email").asText();
        out.shippingAddress = event.get("shippingAddress").asText();
        out.itemsSummary = formatItems(event.get("items"));

        String topic = failed ? paymentFailedTopic : paymentSuccessTopic;
        kafkaTemplate.send(topic, orderId.toString(), objectMapper.writeValueAsString(out));

        System.out.println("""
                
                ============================================================
                PAYMENT SERVICE - RESULT PUBLISHED
                ============================================================
                Topic          : %s
                Order ID       : %s
                Payment ID     : %s
                Result         : %s
                Recipient      : %s
                Correlation ID : %s
                ============================================================
                """.formatted(
                topic,
                orderId,
                payment.getId(),
                payment.getStatus(),
                event.get("email").asText(),
                payment.getCorrelationId()
        ));
    }

    public Payment getLatestByOrderId(Long orderId) {
        return repository.findFirstByOrderIdOrderByIdDesc(orderId).orElseThrow(() -> new EntityNotFoundException("Payment not found for orderId " + orderId));
    }

    public List<Payment> getPaymentsByOrderId(Long orderId) { return repository.findByOrderId(orderId); }
    public List<Payment> getAllPayments() { return repository.findAll(); }

    private String formatItems(JsonNode items) {
        if (items == null || !items.isArray()) return "[]";
        return StreamSupport.stream(items.spliterator(), false)
                .map(item -> item.get("sku").asText() + " x" + item.get("quantity").asText())
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
