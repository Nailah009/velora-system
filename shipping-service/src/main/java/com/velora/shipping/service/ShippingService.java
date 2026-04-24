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
import java.util.List;
import java.util.UUID;

@Service
public class ShippingService {
    private final ShipmentRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.shipment-created}")
    private String shipmentCreatedTopic;

    public ShippingService(ShipmentRepository repository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void createShipmentFromPayment(String message) throws Exception {
        JsonNode event = objectMapper.readTree(message);
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
        out.orderId = shipment.getOrderId();
        out.shipmentId = shipment.getId();
        out.username = event.has("username") ? event.get("username").asText() : null;
        out.email = event.has("email") ? event.get("email").asText() : null;
        out.address = shipment.getAddress();
        out.courier = shipment.getCourier();
        out.trackingNumber = shipment.getTrackingNumber();
        out.status = shipment.getStatus().name();
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

    public Shipment getLatestByOrderId(Long orderId) {
        return repository.findFirstByOrderIdOrderByIdDesc(orderId).orElseThrow(() -> new EntityNotFoundException("Shipment not found for orderId " + orderId));
    }
    public List<Shipment> getAllShipments() { return repository.findAll(); }
}
