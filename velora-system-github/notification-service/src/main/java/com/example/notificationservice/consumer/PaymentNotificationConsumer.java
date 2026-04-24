package com.example.notificationservice.consumer;

import com.example.notificationservice.config.KafkaConfig;
import com.example.notificationservice.event.PaymentResultEvent;
import com.example.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentNotificationConsumer.class);

    private final NotificationService notificationService;

    public PaymentNotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = KafkaConfig.PAYMENT_SUCCESS_TOPIC, groupId = "${spring.application.name}")
    public void onPaymentSuccess(PaymentResultEvent event) {
        log.info("""
============================================================
NOTIFICATION CONSUMER - RECEIVE PAYMENT SUCCESS
============================================================
Queue          : {}
Order ID       : {}
Customer       : {}
Email          : {}
Amount         : {}
Payment Status : {}
============================================================
""",
                KafkaConfig.PAYMENT_SUCCESS_TOPIC,
                event.getOrderId(),
                event.getCustomerName(),
                event.getEmail(),
                event.getAmount(),
                event.getPaymentStatus());

        notificationService.savePaymentNotification(event);
    }

    @KafkaListener(topics = KafkaConfig.PAYMENT_FAILED_TOPIC, groupId = "${spring.application.name}")
    public void onPaymentFailed(PaymentResultEvent event) {
        log.info("""
============================================================
NOTIFICATION CONSUMER - RECEIVE PAYMENT FAILED
============================================================
Queue          : {}
Order ID       : {}
Customer       : {}
Email          : {}
Reason         : {}
Payment Status : {}
============================================================
""",
                KafkaConfig.PAYMENT_FAILED_TOPIC,
                event.getOrderId(),
                event.getCustomerName(),
                event.getEmail(),
                event.getReason(),
                event.getPaymentStatus());

        notificationService.savePaymentNotification(event);
    }
}