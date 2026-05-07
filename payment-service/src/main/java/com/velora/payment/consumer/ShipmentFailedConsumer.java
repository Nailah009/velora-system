package com.velora.payment.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.payment.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ShipmentFailedConsumer {
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public ShipmentFailedConsumer(PaymentService paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.shipment-failed}", groupId = "payment-service-shipment-failed")
    public void onShipmentFailed(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        System.out.println("""
                
                ============================================================
                PAYMENT CONSUMER - RECEIVE SHIPMENT FAILED
                ============================================================
                Topic          : velora.shipment.failed.topic
                Saga ID        : %s
                Order ID       : %s
                Reason         : %s
                Action         : REFUND_PAYMENT as Saga Compensation
                Correlation ID : %s
                ============================================================
                """.formatted(
                event.has("sagaId") ? event.get("sagaId").asText() : "-",
                event.has("orderId") ? event.get("orderId").asText() : "-",
                event.has("reason") ? event.get("reason").asText() : "Shipment failed",
                event.has("correlationId") ? event.get("correlationId").asText() : "-"
        ));
        paymentService.refundPaymentFromShipmentFailed(message);
    }
}
