package com.example.paymentservice.consumer;

import com.example.paymentservice.config.KafkaConfig;
import com.example.paymentservice.event.OrderCreatedEvent;
import com.example.paymentservice.service.PaymentService;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {

    private final PaymentService paymentService;

    public OrderCreatedConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = KafkaConfig.ORDER_TOPIC, groupId = "${spring.application.name}")
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            System.out.println("============================================================");
            System.out.println("PAYMENT CONSUMER - RECEIVE ORDER CREATED");
            System.out.println("============================================================");
            System.out.println("Queue         : " + KafkaConfig.ORDER_TOPIC);
            System.out.println("Order ID      : " + event.getOrderId());
            System.out.println("Customer      : " + event.getCustomerName());
            System.out.println("Amount        : " + event.getTotalAmount());
            System.out.println("Payment Method: " + event.getPaymentMethod());
            System.out.println("============================================================");

            if ("DLQ".equalsIgnoreCase(event.getPaymentMethod())) {
                throw new IllegalArgumentException("Simulasi gagal consumer payment");
            }

            paymentService.processOrderPayment(event);

        } catch (Exception e) {
            System.out.println("PAYMENT CONSUMER - gagal proses orderId=" + event.getOrderId());
            throw new RuntimeException(
                    "Message ditolak dan dikirim ke DLQ", e
            );
        }
    }
}
