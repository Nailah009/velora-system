package com.example.paymentservice.service;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentStatus;
import com.example.paymentservice.event.OrderCreatedEvent;
import com.example.paymentservice.event.PaymentResultEvent;
import com.example.paymentservice.producer.PaymentResultProducer;
import com.example.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentResultProducer paymentResultProducer;

    public PaymentService(PaymentRepository paymentRepository, PaymentResultProducer paymentResultProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentResultProducer = paymentResultProducer;
    }

    public void processOrderPayment(OrderCreatedEvent event) {
        log.info("""
============================================================
PAYMENT SERVICE - START PROCESS PAYMENT
============================================================
Order ID      : {}
Customer      : {}
Email         : {}
Amount        : {}
Payment Method: {}
Items         : {}
============================================================
""",
                event.getOrderId(),
                event.getCustomerName(),
                event.getEmail(),
                event.getTotalAmount(),
                event.getPaymentMethod(),
                event.getItems().stream().map(i -> i.getSku() + " x" + i.getQuantity()).toList());

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setOrderId(event.getOrderId());
        payment.setAmount(event.getTotalAmount());
        payment.setPaymentMethod(event.getPaymentMethod());
        payment.setCreatedAt(LocalDateTime.now());

        boolean success = !"FAIL".equalsIgnoreCase(event.getPaymentMethod());
        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setReason("Payment completed");
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setReason("Payment was simulated as FAILED because paymentMethod=FAIL");
        }

        paymentRepository.save(payment);

        log.info("""
============================================================
PAYMENT SERVICE - PAYMENT SAVED
============================================================
Payment ID    : {}
Order ID      : {}
Method        : {}
Amount        : {}
Status        : {}
Reason        : {}
Created At    : {}
============================================================
""",
                payment.getId(),
                payment.getOrderId(),
                payment.getPaymentMethod(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getReason(),
                payment.getCreatedAt());

        PaymentResultEvent resultEvent = new PaymentResultEvent();
        resultEvent.setOrderId(event.getOrderId());
        resultEvent.setItems(event.getItems());
        resultEvent.setAmount(event.getTotalAmount());
        resultEvent.setPaymentMethod(event.getPaymentMethod());
        resultEvent.setEmail(event.getEmail());
        resultEvent.setCustomerName(event.getCustomerName());
        resultEvent.setPaymentStatus(payment.getStatus().name());
        resultEvent.setReason(payment.getReason());
        resultEvent.setShippingAddress(event.getShippingAddress());

        if (success) {
            paymentResultProducer.sendSuccess(resultEvent);
        } else {
            paymentResultProducer.sendFailure(resultEvent);
        }

        log.info("""
============================================================
PAYMENT SERVICE - RESULT PUBLISHED
============================================================
Order ID      : {}
Routing Key   : payment.{}
Recipient     : {}
============================================================
""",
                event.getOrderId(),
                success ? "success" : "failed",
                event.getEmail());
    }

    public List<Payment> findByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}
