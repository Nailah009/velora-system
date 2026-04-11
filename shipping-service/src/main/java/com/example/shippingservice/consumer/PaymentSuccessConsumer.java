package com.example.shippingservice.consumer;

import com.example.shippingservice.config.RabbitConfig;
import com.example.shippingservice.event.PaymentResultEvent;
import com.example.shippingservice.service.ShippingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentSuccessConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentSuccessConsumer.class);

    private final ShippingService shippingService;

    public PaymentSuccessConsumer(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @RabbitListener(queues = RabbitConfig.PAYMENT_SUCCESS_QUEUE)
    public void handlePaymentSuccess(PaymentResultEvent event) {
        log.info("""
============================================================
SHIPPING CONSUMER - RECEIVE PAYMENT SUCCESS
============================================================
Queue         : {}
Order ID      : {}
Customer      : {}
Payment Status: {}
============================================================
""",
                RabbitConfig.PAYMENT_SUCCESS_QUEUE,
                event.getOrderId(),
                event.getCustomerName(),
                event.getPaymentStatus());
        shippingService.createShipment(event);
    }
}
