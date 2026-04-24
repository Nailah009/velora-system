package com.velora.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.notification.model.NotificationLog;
import com.velora.notification.repository.NotificationLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationLogRepository repository;
    private final ObjectMapper objectMapper;

    public NotificationService(NotificationLogRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveNotification(String eventType, String rawMessage) throws Exception {
        JsonNode event = objectMapper.readTree(rawMessage);
        Long orderId = event.get("orderId").asLong();
        String correlationId = event.has("correlationId") ? event.get("correlationId").asText() : "-";
        String recipient = event.has("email") ? event.get("email").asText() : "-";
        String username = event.has("username") ? event.get("username").asText() : "Customer";
        String message;
        if ("PAYMENT_SUCCESS".equals(eventType)) {
            message = "Hi " + username + ", payment for order " + orderId + " berhasil. Pesanan sedang diproses.";
        } else if ("PAYMENT_FAILED".equals(eventType)) {
            message = "Hi " + username + ", payment for order " + orderId + " gagal. Silakan gunakan metode pembayaran lain.";
        } else {
            message = "Hi " + username + ", shipment for order " + orderId + " sudah dibuat. Courier: "
                    + event.get("courier").asText() + ", Tracking Number: " + event.get("trackingNumber").asText();
        }
        NotificationLog log = new NotificationLog();
        log.setOrderId(orderId);
        log.setCorrelationId(correlationId);
        log.setEventType(eventType);
        log.setMessage(message);
        repository.save(log);

        System.out.println("""
                
                ============================================================
                NOTIFICATION SERVICE - NOTIFICATION SAVED
                ============================================================
                Notification ID: %s
                Order ID       : %s
                Recipient      : %s
                Type           : %s
                Status         : SENT
                Message        : %s
                Correlation ID : %s
                Created At     : %s
                ============================================================
                """.formatted(
                log.getId(),
                orderId,
                recipient,
                eventType,
                message,
                correlationId,
                log.getCreatedAt()
        ));
    }

    public List<NotificationLog> getByOrderId(Long orderId) { return repository.findByOrderIdOrderByIdDesc(orderId); }
    public List<NotificationLog> getAll() { return repository.findAll(); }
}
