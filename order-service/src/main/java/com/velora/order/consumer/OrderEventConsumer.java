package com.velora.order.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.order.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {
    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    public OrderEventConsumer(ObjectMapper objectMapper, OrderService orderService) {
        this.objectMapper = objectMapper;
        this.orderService = orderService;
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-success}", groupId = "order-service-payment-success")
    public void onPaymentSuccess(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        Long orderId = event.get("orderId").asLong();
        orderService.markPaymentSuccess(orderId);
        printStatusUpdate("ORDER SERVICE - RECEIVE PAYMENT SUCCESS", orderId, "PAID", event);
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-failed}", groupId = "order-service-payment-failed")
    public void onPaymentFailed(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        Long orderId = event.get("orderId").asLong();
        orderService.markPaymentFailed(orderId);
        printStatusUpdate("ORDER SERVICE - RECEIVE PAYMENT FAILED", orderId, "PAYMENT_FAILED", event);
    }

    @KafkaListener(topics = "${app.kafka.topics.shipment-created}", groupId = "order-service-shipment-created")
    public void onShipmentCreated(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        Long orderId = event.get("orderId").asLong();
        orderService.markReadyToShip(orderId);
        printStatusUpdate("ORDER SERVICE - RECEIVE SHIPMENT CREATED", orderId, "READY_TO_SHIP", event);
    }

    private void printStatusUpdate(String title, Long orderId, String newStatus, JsonNode event) {
        System.out.println("""
                
                ============================================================
                %s
                ============================================================
                Order ID       : %s
                New Status     : %s
                Event Status   : %s
                Correlation ID : %s
                ============================================================
                """.formatted(
                title,
                orderId,
                newStatus,
                event.has("status") ? event.get("status").asText() : "-",
                event.has("correlationId") ? event.get("correlationId").asText() : "-"
        ));
    }
}
