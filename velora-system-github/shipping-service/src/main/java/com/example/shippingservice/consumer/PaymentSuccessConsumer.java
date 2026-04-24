package com.example.shippingservice.consumer;

import com.example.shippingservice.config.KafkaConfig;
import com.example.shippingservice.event.PaymentResultEvent;
import com.example.shippingservice.service.ShippingService;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentSuccessConsumer {

    private final ShippingService shippingService;

    public PaymentSuccessConsumer(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @KafkaListener(topics = KafkaConfig.PAYMENT_SUCCESS_TOPIC, groupId = "${spring.application.name}")
    public void handlePaymentSuccess(PaymentResultEvent event) {
        try {
            System.out.println("============================================================");
            System.out.println("SHIPPING CONSUMER - RECEIVE PAYMENT SUCCESS");
            System.out.println("============================================================");
            System.out.println("Queue          : " + KafkaConfig.PAYMENT_SUCCESS_TOPIC);
            System.out.println("Order ID       : " + event.getOrderId());
            System.out.println("Customer       : " + event.getCustomerName());
            System.out.println("Payment Status : " + event.getPaymentStatus());
            System.out.println("Address        : " + event.getShippingAddress());
            System.out.println("============================================================");

            if ("FAIL-SHIPPING".equalsIgnoreCase(event.getShippingAddress())) {
                throw new IllegalArgumentException("Simulasi gagal shipping consumer");
            }

            shippingService.createShipment(event);

        } catch (Exception e) {
            System.out.println("SHIPPING CONSUMER - gagal proses orderId=" + event.getOrderId());
            throw new RuntimeException(
                    "Message ditolak dan dikirim ke DLQ", e
            );
        }
    }
}