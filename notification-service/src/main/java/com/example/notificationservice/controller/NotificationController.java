package com.example.notificationservice.controller;

import com.example.notificationservice.entity.NotificationLog;
import com.example.notificationservice.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/reference/{referenceId}")
    public List<NotificationLog> getByReferenceId(@PathVariable String referenceId) {
        return notificationService.findByReferenceId(referenceId);
    }
}
