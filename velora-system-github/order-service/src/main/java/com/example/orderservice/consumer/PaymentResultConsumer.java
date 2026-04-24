package com.example.orderservice.consumer;

import com.example.orderservice.config.KafkaConfig;
import com.example.orderservice.event.PaymentResultEvent;
import com.example.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentResultConsumer.class);

    private final OrderService orderService;

    public PaymentResultConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = KafkaConfig.PAYMENT_SUCCESS_TOPIC, groupId = "${spring.application.name}")
    public void handlePaymentSuccess(PaymentResultEvent event) {
        log.info("""
============================================================
ORDER CONSUMER - RECEIVE PAYMENT SUCCESS
============================================================
Queue         : {}
Order ID      : {}
Customer      : {}
Payment Status: {}
Amount        : {}
============================================================
""",
                KafkaConfig.PAYMENT_SUCCESS_TOPIC,
                event.getOrderId(),
                event.getCustomerName(),
                event.getPaymentStatus(),
                event.getAmount());
        orderService.updateFromPaymentResult(event);
    }

    @KafkaListener(topics = KafkaConfig.PAYMENT_FAILED_TOPIC, groupId = "${spring.application.name}")
    public void handlePaymentFailed(PaymentResultEvent event) {
        log.info("""
============================================================
ORDER CONSUMER - RECEIVE PAYMENT FAILED
============================================================
Queue         : {}
Order ID      : {}
Customer      : {}
Payment Status: {}
Reason        : {}
============================================================
""",
                KafkaConfig.PAYMENT_FAILED_TOPIC,
                event.getOrderId(),
                event.getCustomerName(),
                event.getPaymentStatus(),
                event.getReason());
        orderService.updateFromPaymentResult(event);
    }
}
