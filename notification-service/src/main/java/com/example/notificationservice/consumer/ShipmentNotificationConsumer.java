package com.example.notificationservice.consumer;

import com.example.notificationservice.config.RabbitConfig;
import com.example.notificationservice.event.ShipmentCreatedEvent;
import com.example.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ShipmentNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(ShipmentNotificationConsumer.class);

    private final NotificationService notificationService;

    public ShipmentNotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitConfig.SHIPMENT_CREATED_QUEUE)
    public void onShipmentCreated(ShipmentCreatedEvent event) {
        log.info("""
============================================================
NOTIFICATION CONSUMER - RECEIVE SHIPMENT CREATED
============================================================
Queue         : {}
Order ID      : {}
Customer      : {}
Email         : {}
Tracking No   : {}
Courier       : {}
============================================================
""",
                RabbitConfig.SHIPMENT_CREATED_QUEUE,
                event.getOrderId(),
                event.getCustomerName(),
                event.getEmail(),
                event.getTrackingNumber(),
                event.getCourier());

        notificationService.saveShipmentNotification(event);
    }
}