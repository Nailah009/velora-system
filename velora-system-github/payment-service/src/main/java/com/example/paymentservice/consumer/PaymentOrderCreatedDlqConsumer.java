package com.example.paymentservice.consumer;



import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentOrderCreatedDlqConsumer {

    @KafkaListener(topics = "payment.order.created.dlq", groupId = "${spring.application.name}")
    public void handleDlq(Object message) {
        System.out.println("============================================================");
        System.out.println("PAYMENT DLQ CONSUMER");
        System.out.println("============================================================");
        System.out.println("Pesan masuk Dead Letter Queue");
        System.out.println("Queue   : payment.order.created.dlq");
        System.out.println("Body    : " + message.toString());
        System.out.println("============================================================");
    }
}
