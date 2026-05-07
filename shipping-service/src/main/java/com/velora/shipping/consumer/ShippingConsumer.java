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
                Topic          : velora.payment.success.topic
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
                event.has("status") ? event.get("status").asText() : "-",
                address,
                event.has("correlationId") ? event.get("correlationId").asText() : "-"
        ));

        /*
         * SAGA BUSINESS FAILURE
         * Jangan throw exception di sini.
         * Kalau throw exception, Kafka menganggap consumer error teknis dan event masuk DLQ.
         */
        if ("SAGA_FAIL_SHIPPING".equalsIgnoreCase(address)) {
            System.out.println("""
                    
                    ============================================================
                    SHIPPING CONSUMER - SAGA SHIPPING FAILED
                    ============================================================
                    Order ID       : %s
                    Customer       : %s
                    Reason         : Shipping rejected by Saga simulation
                    Action         : Publish Shipment Failed Event
                    ============================================================
                    """.formatted(
                    orderId,
                    event.has("username") ? event.get("username").asText() : "-"
            ));

            shippingService.publishShipmentFailedFromPayment(message, "Shipping rejected by Saga simulation");
            return;
        }

        /*
         * DLQ TECHNICAL FAILURE
         * Ini sengaja throw exception agar masuk Dead Letter Topic.
         */
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
