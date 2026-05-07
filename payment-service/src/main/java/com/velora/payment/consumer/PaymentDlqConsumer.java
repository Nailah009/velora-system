package com.velora.payment.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentDlqConsumer {
    private final ObjectMapper objectMapper;

    public PaymentDlqConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-dlq:velora.order.topic.DLQ}", groupId = "payment-dlq-consumer")
    public void onPaymentDlq(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        System.out.println("""
                
                ============================================================
                PAYMENT DLQ CONSUMER
                ============================================================
                Pesan masuk Dead Letter Queue
                Queue          : velora.order.topic.DLQ
                Order ID       : %s
                Customer       : %s
                Email          : %s
                Amount         : %s
                Payment Method : %s
                Reason         : Payment consumer failed after retry
                Body           : %s
                ============================================================
                """.formatted(
                event.has("orderId") ? event.get("orderId").asText() : "-",
                event.has("username") ? event.get("username").asText() : "-",
                event.has("email") ? event.get("email").asText() : "-",
                event.has("totalAmount") ? event.get("totalAmount").asText() : "-",
                event.has("paymentMethod") ? event.get("paymentMethod").asText() : "-",
                event.toString()
        ));
    }
}
