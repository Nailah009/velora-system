package com.velora.shipping.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ShippingDlqConsumer {
    private final ObjectMapper objectMapper;

    public ShippingDlqConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-success-dlq:velora.payment.success.topic.DLQ}", groupId = "shipping-dlq-consumer")
    public void onShippingDlq(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        System.out.println("""
                
                ============================================================
                SHIPPING DLQ CONSUMER
                ============================================================
                Pesan masuk Dead Letter Queue
                Queue          : velora.payment.success.topic.DLQ
                Order ID       : %s
                Customer       : %s
                Email          : %s
                Payment Status : %s
                Address        : %s
                Reason         : Shipping consumer failed after retry
                Body           : %s
                ============================================================
                """.formatted(
                event.has("orderId") ? event.get("orderId").asText() : "-",
                event.has("username") ? event.get("username").asText() : "-",
                event.has("email") ? event.get("email").asText() : "-",
                event.has("status") ? event.get("status").asText() : "-",
                event.has("shippingAddress") ? event.get("shippingAddress").asText() : "-",
                event.toString()
        ));
    }
}
