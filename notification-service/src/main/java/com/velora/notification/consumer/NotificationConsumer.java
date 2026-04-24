package com.velora.notification.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-success}", groupId = "notification-payment-success")
    public void onPaymentSuccess(String message) throws Exception {
        printIncoming("NOTIFICATION CONSUMER - RECEIVE PAYMENT SUCCESS", "notification.payment.success.queue", message);
        notificationService.saveNotification("PAYMENT_SUCCESS", message);
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-failed}", groupId = "notification-payment-failed")
    public void onPaymentFailed(String message) throws Exception {
        printIncoming("NOTIFICATION CONSUMER - RECEIVE PAYMENT FAILED", "notification.payment.failed.queue", message);
        notificationService.saveNotification("PAYMENT_FAILED", message);
    }

    @KafkaListener(topics = "${app.kafka.topics.shipment-created}", groupId = "notification-shipment-created")
    public void onShipmentCreated(String message) throws Exception {
        printIncoming("NOTIFICATION CONSUMER - RECEIVE SHIPMENT CREATED", "notification.shipment.created.queue", message);
        notificationService.saveNotification("SHIPMENT_CREATED", message);
    }

    private void printIncoming(String title, String queue, String rawMessage) throws Exception {
        JsonNode event = objectMapper.readTree(rawMessage);
        System.out.println("""
                
                ============================================================
                %s
                ============================================================
                Queue          : %s
                Order ID       : %s
                Customer       : %s
                Email          : %s
                Status         : %s
                Correlation ID : %s
                ============================================================
                """.formatted(
                title,
                queue,
                event.has("orderId") ? event.get("orderId").asText() : "-",
                event.has("username") ? event.get("username").asText() : "-",
                event.has("email") ? event.get("email").asText() : "-",
                event.has("status") ? event.get("status").asText() : "-",
                event.has("correlationId") ? event.get("correlationId").asText() : "-"
        ));
    }
}
