package com.velora.notification.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notification_logs")
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private String correlationId;
    private String eventType;
    @Column(length = 1000)
    private String message;
    private Instant createdAt;

    @PrePersist
    void prePersist() { createdAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
