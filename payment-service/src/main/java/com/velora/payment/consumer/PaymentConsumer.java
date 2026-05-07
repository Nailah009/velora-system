package com.velora.payment.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.payment.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public PaymentConsumer(PaymentService paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-created}", groupId = "payment-service")
    public void onOrderCreated(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        String orderId = event.get("orderId").asText();
        String paymentMethod = event.get("paymentMethod").asText();

        System.out.println("""
                
                ============================================================
                PAYMENT CONSUMER - RECEIVE ORDER CREATED
                ============================================================
                Queue          : velora.order.topic
                Order ID       : %s
                Customer       : %s
                Email          : %s
                Amount         : %s
                Payment Method : %s
                Correlation ID : %s
                ============================================================
                """.formatted(
                orderId,
                event.get("username").asText(),
                event.get("email").asText(),
                event.get("totalAmount").asText(),
                paymentMethod,
                event.get("correlationId").asText()
        ));

        if ("DLQ".equalsIgnoreCase(paymentMethod)) {
            System.out.println("""
                    
                    ============================================================
                    PAYMENT CONSUMER - GAGAL PROSES ORDER
                    ============================================================
                    Order ID       : %s
                    Customer       : %s
                    Reason         : Forced technical failure for DLQ testing
                    Action         : Throw exception, retry, then send to DLQ
                    ============================================================
                    """.formatted(
                    orderId,
                    event.get("username").asText()
            ));
            throw new RuntimeException("Forced DLQ test from payment consumer for orderId=" + orderId);
        }

        paymentService.processOrderEvent(message);
    }
}
