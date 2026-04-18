package com.example.shippingservice.controller;

import com.example.shippingservice.entity.Shipment;
import com.example.shippingservice.service.ShippingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shipments")
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @GetMapping("/order/{orderId}")
    public Shipment getByOrderId(@PathVariable String orderId) {
        return shippingService.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found for order: " + orderId));
    }
}
