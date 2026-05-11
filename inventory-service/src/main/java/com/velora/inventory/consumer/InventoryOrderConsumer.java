package com.velora.inventory.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.inventory.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryOrderConsumer {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    public InventoryOrderConsumer(InventoryService inventoryService, ObjectMapper objectMapper) {
        this.inventoryService = inventoryService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-created}", groupId = "inventory-service-order-created")
    public void onOrderCreated(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);

        System.out.println("""
        ============================================================
        INVENTORY CONSUMER - RECEIVE ORDER CREATED
        ============================================================
        Topic          : velora.order.topic
        Saga ID        : %s
        Order ID       : %s
        Customer       : %s
        Action         : RESERVE_STOCK_BY_KAFKA
        Correlation ID : %s
        ============================================================
        """.formatted(
                event.has("sagaId") ? event.get("sagaId").asText() : "-",
                event.has("orderId") ? event.get("orderId").asText() : "-",
                event.has("username") ? event.get("username").asText() : "-",
                event.has("correlationId") ? event.get("correlationId").asText() : "-"
        ));

        inventoryService.reserveStockFromOrderEvent(message);
    }
}
