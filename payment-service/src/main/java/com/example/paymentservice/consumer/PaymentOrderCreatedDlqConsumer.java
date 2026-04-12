package com.example.paymentservice.consumer;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentOrderCreatedDlqConsumer {

    @RabbitListener(
            queues = "payment.order.created.dlq",
            containerFactory = "rawRabbitListenerContainerFactory"
    )
    public void handleDlq(Message message) {
        System.out.println("============================================================");
        System.out.println("PAYMENT DLQ CONSUMER");
        System.out.println("============================================================");
        System.out.println("Pesan masuk Dead Letter Queue");
        System.out.println("Queue   : payment.order.created.dlq");
        System.out.println("Body    : " + new String(message.getBody()));
        System.out.println("============================================================");
    }
}
