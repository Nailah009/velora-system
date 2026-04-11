package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.InventoryReserveResponse;
import com.example.inventoryservice.dto.ProductVariantResponse;
import com.example.inventoryservice.dto.ReserveInventoryRequest;
import com.example.inventoryservice.entity.Product;
import com.example.inventoryservice.entity.ProductVariant;
import com.example.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/products")
    public List<Product> getProducts() {
        return inventoryService.getAllProducts();
    }

    @GetMapping("/products/{productId}/variants")
    public List<ProductVariant> getVariants(@PathVariable String productId) {
        return inventoryService.getVariantsByProduct(productId);
    }

    @GetMapping("/variants/{sku}")
    public ProductVariantResponse getVariant(@PathVariable String sku) {
        return inventoryService.getVariantResponse(sku);
    }

    @PostMapping("/inventory/reserve")
    public InventoryReserveResponse reserveStock(@Valid @RequestBody ReserveInventoryRequest request) {
        return inventoryService.reserveStock(request);
    }
}
