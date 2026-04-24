package com.velora.shipping.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.shipping.service.ShippingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ShippingConsumer {
    private final ShippingService shippingService;
    private final ObjectMapper objectMapper;

    public ShippingConsumer(ShippingService shippingService, ObjectMapper objectMapper) {
        this.shippingService = shippingService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-success}", groupId = "shipping-service")
    public void onPaymentSuccess(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        String orderId = event.get("orderId").asText();
        String address = event.get("shippingAddress").asText();

        System.out.println("""
                
                ============================================================
                SHIPPING CONSUMER - RECEIVE PAYMENT SUCCESS
                ============================================================
                Queue          : velora.payment.success.topic
                Order ID       : %s
                Customer       : %s
                Email          : %s
                Payment Status : %s
                Address        : %s
                Correlation ID : %s
                ============================================================
                """.formatted(
                orderId,
                event.has("username") ? event.get("username").asText() : "-",
                event.has("email") ? event.get("email").asText() : "-",
                event.get("status").asText(),
                address,
                event.get("correlationId").asText()
        ));

        if ("FAIL-SHIPPING".equalsIgnoreCase(address)) {
            System.out.println("""
                    
                    ============================================================
                    SHIPPING CONSUMER - GAGAL PROSES SHIPMENT
                    ============================================================
                    Order ID       : %s
                    Customer       : %s
                    Reason         : Forced shipping failure for DLQ testing
                    Action         : Throw exception, retry, then send to DLQ
                    ============================================================
                    """.formatted(
                    orderId,
                    event.has("username") ? event.get("username").asText() : "-"
            ));
            throw new RuntimeException("Forced DLQ test from shipping consumer for orderId=" + orderId);
        }

        shippingService.createShipmentFromPayment(message);
    }
}
