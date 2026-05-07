package com.velora.inventory.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.inventory.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SagaCompensationConsumer {
    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    public SagaCompensationConsumer(InventoryService inventoryService, ObjectMapper objectMapper) {
        this.inventoryService = inventoryService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-failed}", groupId = "inventory-service-payment-failed")
    public void onPaymentFailed(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        printIncoming("INVENTORY CONSUMER - RECEIVE PAYMENT FAILED", "velora.payment.failed.topic", event, "Payment failed, release stock");
        inventoryService.releaseStockFromSagaEvent(event, "Payment failed");
    }

    @KafkaListener(topics = "${app.kafka.topics.shipment-failed}", groupId = "inventory-service-shipment-failed")
    public void onShipmentFailed(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        printIncoming("INVENTORY CONSUMER - RECEIVE SHIPMENT FAILED", "velora.shipment.failed.topic", event, "Shipment failed, release stock");
        inventoryService.releaseStockFromSagaEvent(event, "Shipment failed");
    }

    private void printIncoming(String title, String topic, JsonNode event, String action) {
        System.out.println("""
                
                ============================================================
                %s
                ============================================================
                Topic          : %s
                Saga ID        : %s
                Order ID       : %s
                Action         : %s
                Correlation ID : %s
                ============================================================
                """.formatted(
                title,
                topic,
                event.has("sagaId") ? event.get("sagaId").asText() : "-",
                event.has("orderId") ? event.get("orderId").asText() : "-",
                action,
                event.has("correlationId") ? event.get("correlationId").asText() : "-"
        ));
    }
}
