package com.velora.shipping.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "shipments")
public class Shipment {
    public enum Status { SHIPPED }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private String correlationId;
    private String address;
    private String courier;
    private String trackingNumber;
    @Enumerated(EnumType.STRING)
    private Status status;
    private Instant createdAt;

    @PrePersist
    void prePersist() { createdAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCourier() { return courier; }
    public void setCourier(String courier) { this.courier = courier; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
