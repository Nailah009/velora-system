package com.example.notificationservice.consumer;

import com.example.notificationservice.config.RabbitConfig;
import com.example.notificationservice.event.PaymentResultEvent;
import com.example.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentNotificationConsumer.class);

    private final NotificationService notificationService;

    public PaymentNotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitConfig.PAYMENT_SUCCESS_QUEUE)
    public void onPaymentSuccess(PaymentResultEvent event) {
        log.info("""
============================================================
NOTIFICATION CONSUMER - RECEIVE PAYMENT SUCCESS
============================================================
Queue         : {}
Order ID      : {}
Customer      : {}
Amount        : {}
============================================================
""",
                RabbitConfig.PAYMENT_SUCCESS_QUEUE,
                event.getOrderId(),
                event.getCustomerName(),
                event.getAmount());
        notificationService.savePaymentNotification(event);
    }

    @RabbitListener(queues = RabbitConfig.PAYMENT_FAILED_QUEUE)
    public void onPaymentFailed(PaymentResultEvent event) {
        log.info("""
============================================================
NOTIFICATION CONSUMER - RECEIVE PAYMENT FAILED
============================================================
Queue         : {}
Order ID      : {}
Customer      : {}
Reason        : {}
============================================================
""",
                RabbitConfig.PAYMENT_FAILED_QUEUE,
                event.getOrderId(),
                event.getCustomerName(),
                event.getReason());
        notificationService.savePaymentNotification(event);
    }
}
