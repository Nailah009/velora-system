package com.example.shippingservice.controller;

import com.example.shippingservice.entity.Shipment;
import com.example.shippingservice.service.ShippingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @GetMapping("/order/{orderId}")
    public List<Shipment> getByOrderId(@PathVariable String orderId) {
        return shippingService.findByOrderId(orderId);
    }
}
