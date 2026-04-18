package com.example.notificationservice.service;

    import com.example.notificationservice.entity.NotificationLog;
    import com.example.notificationservice.event.PaymentResultEvent;
    import com.example.notificationservice.event.ShipmentCreatedEvent;
    import com.example.notificationservice.repository.NotificationRepository;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.stereotype.Service;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.UUID;

    @Service
    public class NotificationService {

        private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

        private final NotificationRepository notificationRepository;

        public NotificationService(NotificationRepository notificationRepository) {
            this.notificationRepository = notificationRepository;
        }

        public void savePaymentNotification(PaymentResultEvent event) {
            NotificationLog notification = new NotificationLog();
            notification.setId(UUID.randomUUID().toString());
            notification.setOrderId(event.getOrderId());
            notification.setPaymentId(event.getPaymentId());
            notification.setRecipient(event.getEmail());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setStatus("SENT");

            if ("SUCCESS".equalsIgnoreCase(event.getPaymentStatus())) {
                notification.setType("PAYMENT_SUCCESS");
                notification.setMessage("Hi " + event.getCustomerName() + ", payment for order " + event.getOrderId() + " berhasil. Pesanan sedang diproses.");
            } else {
                notification.setType("PAYMENT_FAILED");
                notification.setMessage("Hi " + event.getCustomerName() + ", payment for order " + event.getOrderId() + " gagal. Reason: " + event.getReason());
            }

            notificationRepository.save(notification);
            logNotification(notification);
        }

        public void saveShipmentNotification(ShipmentCreatedEvent event) {
            NotificationLog notification = new NotificationLog();
            notification.setId(UUID.randomUUID().toString());
            notification.setOrderId(event.getOrderId());
            notification.setShipmentId(event.getShipmentId());
            notification.setRecipient(event.getEmail());
            notification.setType("SHIPMENT_CREATED");
            notification.setStatus("SENT");
            notification.setMessage("Hi " + event.getCustomerName() + ", shipment for order " + event.getOrderId()
                    + " sudah dibuat. Courier: " + event.getCourier() + ", Tracking Number: " + event.getTrackingNumber());
            notification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(notification);
            logNotification(notification);
        }

        public List<NotificationLog> findByOrderId(String orderId) {
            return notificationRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        }

        private void logNotification(NotificationLog notification) {
            log.info("""
============================================================
NOTIFICATION SERVICE - NOTIFICATION SAVED
============================================================
Notification ID: {}
Order ID       : {}
Payment ID     : {}
Shipment ID    : {}
Recipient      : {}
Type           : {}
Status         : {}
Message        : {}
Created At     : {}
============================================================
""",
                    notification.getId(),
                    notification.getOrderId(),
                    notification.getPaymentId(),
                    notification.getShipmentId(),
                    notification.getRecipient(),
                    notification.getType(),
                    notification.getStatus(),
                    notification.getMessage(),
                    notification.getCreatedAt());
        }
    }
