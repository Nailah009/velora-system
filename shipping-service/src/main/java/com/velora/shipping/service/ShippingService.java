package com.velora.shipping.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.shipping.event.ShipmentEvent;
import com.velora.shipping.model.Shipment;
import com.velora.shipping.repository.ShipmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ShippingService {
    private final ShipmentRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.shipment-created}")
    private String shipmentCreatedTopic;

    @Value("${app.kafka.topics.shipment-failed}")
    private String shipmentFailedTopic;

    public ShippingService(ShipmentRepository repository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void createShipmentFromPayment(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
        String address = event.get("shippingAddress").asText();

        Shipment shipment = new Shipment();
        shipment.setOrderId(event.get("orderId").asLong());
        shipment.setCorrelationId(event.get("correlationId").asText());
        shipment.setAddress(event.get("shippingAddress").asText());
        shipment.setCourier("JNE");
        shipment.setTrackingNumber("VLR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        shipment.setStatus(Shipment.Status.SHIPPED);
        repository.save(shipment);

        System.out.println("""
                
                ============================================================
                SHIPPING SERVICE - CREATE SHIPMENT
                ============================================================
                Shipment ID    : %s
                Order ID       : %s
                Recipient      : %s
                Courier        : %s
                Tracking No    : %s
                Address        : %s
                Status         : %s
                Correlation ID : %s
                ============================================================
                """.formatted(
                shipment.getId(),
                shipment.getOrderId(),
                event.has("username") ? event.get("username").asText() : event.get("email").asText(),
                shipment.getCourier(),
                shipment.getTrackingNumber(),
                shipment.getAddress(),
                shipment.getStatus(),
                shipment.getCorrelationId()
        ));

        ShipmentEvent out = new ShipmentEvent();
        out.eventId = UUID.randomUUID().toString();
        out.occurredAt = Instant.now();
        out.correlationId = shipment.getCorrelationId();
        out.sagaId = event.has("sagaId") ? event.get("sagaId").asText() : "SAGA-" + java.time.Year.now().getValue() + "-" + String.format("%04d", shipment.getOrderId());
        out.orderId = shipment.getOrderId();
        out.shipmentId = shipment.getId();
        out.username = event.has("username") ? event.get("username").asText() : null;
        out.email = event.has("email") ? event.get("email").asText() : null;
        out.address = shipment.getAddress();
        out.courier = shipment.getCourier();
        out.trackingNumber = shipment.getTrackingNumber();
        out.status = shipment.getStatus().name();
        out.reason = "Shipment created successfully";
        out.items = event.has("items") ? mapItems(event.get("items")) : new ArrayList<>();
        System.out.println("""
                
                ============================================================
                SAGA - SHIPMENT STEP SUCCESS
                ============================================================
                Saga ID        : %s
                Order ID       : %s
                Step           : CREATE_SHIPMENT
                Status         : SUCCESS
                Next Step      : SEND_NOTIFICATION
                ============================================================
                """.formatted(out.sagaId, shipment.getOrderId()));

        kafkaTemplate.send(shipmentCreatedTopic, shipment.getOrderId().toString(), objectMapper.writeValueAsString(out));

        System.out.println("""
                
                ============================================================
                SHIPPING SERVICE - SHIPMENT CREATED EVENT PUBLISHED
                ============================================================
                Topic          : %s
                Order ID       : %s
                Shipment ID    : %s
                Tracking No    : %s
                Correlation ID : %s
                ============================================================
                """.formatted(
                shipmentCreatedTopic,
                shipment.getOrderId(),
                shipment.getId(),
                shipment.getTrackingNumber(),
                shipment.getCorrelationId()
        ));
    }

    public void publishShipmentFailedFromPayment(String message, String reason) {
        try {
            JsonNode event = objectMapper.readTree(message);
            Long orderId = event.get("orderId").asLong();
            String correlationId = event.has("correlationId") ? event.get("correlationId").asText() : "-";
            String sagaId = event.has("sagaId")
                    ? event.get("sagaId").asText()
                    : "SAGA-" + java.time.Year.now().getValue() + "-" + String.format("%04d", orderId);

            ShipmentEvent out = new ShipmentEvent();
            out.eventId = UUID.randomUUID().toString();
            out.occurredAt = Instant.now();
            out.correlationId = correlationId;
            out.sagaId = sagaId;
            out.orderId = orderId;
            out.shipmentId = null;
            out.username = event.has("username") ? event.get("username").asText() : null;
            out.email = event.has("email") ? event.get("email").asText() : null;
            out.address = event.has("shippingAddress") ? event.get("shippingAddress").asText() : null;
            out.courier = "-";
            out.trackingNumber = "-";
            out.status = "FAILED";
            out.reason = reason;
            out.items = event.has("items") ? mapItems(event.get("items")) : new ArrayList<>();

            String payload = objectMapper.writeValueAsString(out);

            System.out.println("""
                    
                    ============================================================
                    SHIPPING SERVICE - SHIPMENT FAILED EVENT READY
                    ============================================================
                    Topic          : %s
                    Saga ID        : %s
                    Order ID       : %s
                    Reason         : %s
                    Payload        : %s
                    ============================================================
                    """.formatted(
                    shipmentFailedTopic,
                    sagaId,
                    orderId,
                    reason,
                    payload
            ));

            kafkaTemplate.send(shipmentFailedTopic, orderId.toString(), payload);

            System.out.println("""
                    
                    ============================================================
                    SHIPPING SERVICE - SAGA FAILURE PUBLISHED
                    ============================================================
                    Topic          : %s
                    Saga ID        : %s
                    Order ID       : %s
                    Step           : CREATE_SHIPMENT
                    Status         : FAILED
                    Next Step      : REFUND_PAYMENT + RELEASE_STOCK + CANCEL_ORDER
                    Reason         : %s
                    Correlation ID : %s
                    ============================================================
                    """.formatted(
                    shipmentFailedTopic,
                    sagaId,
                    orderId,
                    reason,
                    correlationId
            ));
        } catch (Exception e) {
            /*
             * Safety guard: Saga business failure tidak boleh dilempar lagi ke Kafka listener,
             * karena kalau exception keluar dari consumer, Kafka akan mengirim pesan ke DLQ.
             */
            System.out.println("""
                    
                    ============================================================
                    SHIPPING SERVICE - SAGA FAILURE PUBLISH ERROR
                    ============================================================
                    Reason         : %s
                    Action         : Error logged, message will NOT be sent to DLQ
                    ============================================================
                    """.formatted(e.getMessage()));
        }
    }

    private List<ShipmentEvent.Item> mapItems(JsonNode items) {
        List<ShipmentEvent.Item> mapped = new ArrayList<>();
        if (items == null || !items.isArray()) return mapped;
        for (JsonNode item : items) {
            ShipmentEvent.Item out = new ShipmentEvent.Item();
            out.sku = item.has("sku") ? item.get("sku").asText() : null;
            out.productName = item.has("productName") ? item.get("productName").asText() : null;
            out.quantity = item.has("quantity") ? item.get("quantity").asInt() : 0;
            out.price = item.has("price") ? item.get("price").decimalValue() : BigDecimal.ZERO;
            out.subtotal = item.has("subtotal") ? item.get("subtotal").decimalValue() : BigDecimal.ZERO;
            mapped.add(out);
        }
        return mapped;
    }

    public Shipment getLatestByOrderId(Long orderId) {
        return repository.findFirstByOrderIdOrderByIdDesc(orderId).orElseThrow(() -> new EntityNotFoundException("Shipment not found for orderId " + orderId));
    }
    public List<Shipment> getAllShipments() { return repository.findAll(); }
}
