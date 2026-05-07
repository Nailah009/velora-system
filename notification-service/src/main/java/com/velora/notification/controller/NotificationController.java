package com.velora.notification.controller;

import com.velora.notification.common.ApiResponse;
import com.velora.notification.model.NotificationLog;
import com.velora.notification.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    public NotificationController(NotificationService notificationService) { this.notificationService = notificationService; }

    @GetMapping
    public ApiResponse<List<NotificationLog>> getNotifications() {
        return ApiResponse.success(200, "Notifications retrieved successfully", notificationService.getAll());
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<List<NotificationLog>> getByOrder(@PathVariable Long orderId) {
        return ApiResponse.success(200, "Notifications retrieved successfully", notificationService.getByOrderId(orderId));
    }
}
