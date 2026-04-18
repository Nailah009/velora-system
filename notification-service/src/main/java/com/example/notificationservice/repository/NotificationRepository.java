package com.example.notificationservice.repository;

import com.example.notificationservice.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationLog, String> {
    List<NotificationLog> findByOrderIdOrderByCreatedAtAsc(String orderId);
}
