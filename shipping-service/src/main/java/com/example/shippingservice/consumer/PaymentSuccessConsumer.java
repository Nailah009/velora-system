package com.example.shippingservice.consumer;

import com.example.shippingservice.config.RabbitConfig;
import com.example.shippingservice.event.PaymentResultEvent;
import com.example.shippingservice.service.ShippingService;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentSuccessConsumer {

    private final ShippingService shippingService;

    public PaymentSuccessConsumer(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @RabbitListener(queues = RabbitConfig.PAYMENT_SUCCESS_QUEUE)
    public void handlePaymentSuccess(PaymentResultEvent event) {
        try {
            System.out.println("============================================================");
            System.out.println("SHIPPING CONSUMER - RECEIVE PAYMENT SUCCESS");
            System.out.println("============================================================");
            System.out.println("Queue          : " + RabbitConfig.PAYMENT_SUCCESS_QUEUE);
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
            System.out.println("============================================================");
            System.out.println("SHIPPING CONSUMER - gagal proses orderId=" + event.getOrderId());
            System.out.println("============================================================");
            throw new AmqpRejectAndDontRequeueException(
                    "Message ditolak dan dikirim ke DLQ", e
            );
        }
    }
}