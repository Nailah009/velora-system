package com.example.shippingservice.consumer;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentSuccessDlqConsumer {

    @RabbitListener(
            queues = "shipping.payment.success.dlq",
            containerFactory = "rawRabbitListenerContainerFactory"
    )
    public void handleDlq(Message message) {
        System.out.println("============================================================");
        System.out.println("SHIPPING DLQ CONSUMER");
        System.out.println("============================================================");
        System.out.println("Pesan masuk Dead Letter Queue");
        System.out.println("Queue   : shipping.payment.success.dlq");
        System.out.println("Body    : " + new String(message.getBody()));
        System.out.println("============================================================");
    }
}