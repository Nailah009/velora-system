package com.example.shippingservice.consumer;



import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentSuccessDlqConsumer {

    @KafkaListener(topics = "shipping.payment.success.dlq", groupId = "${spring.application.name}")
    public void handleDlq(Object message) {
        System.out.println("============================================================");
        System.out.println("SHIPPING DLQ CONSUMER");
        System.out.println("============================================================");
        System.out.println("Pesan masuk Dead Letter Queue");
        System.out.println("Queue   : shipping.payment.success.dlq");
        System.out.println("Body    : " + message.toString());
        System.out.println("============================================================");
    }
}