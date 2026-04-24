package com.velora.shipping.controller;

import com.velora.shipping.common.ApiResponse;
import com.velora.shipping.model.Shipment;
import com.velora.shipping.service.ShippingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {
    private final ShippingService shippingService;
    public ShipmentController(ShippingService shippingService) { this.shippingService = shippingService; }

    @GetMapping
    public ApiResponse<List<Shipment>> getShipments() {
        return ApiResponse.success(200, "Shipments retrieved successfully", shippingService.getAllShipments());
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<Shipment> getShipmentByOrder(@PathVariable Long orderId) {
        return ApiResponse.success(200, "Shipment retrieved successfully", shippingService.getLatestByOrderId(orderId));
    }
}
