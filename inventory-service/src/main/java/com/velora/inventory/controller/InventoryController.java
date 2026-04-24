package com.velora.inventory.controller;

import com.velora.inventory.common.ApiResponse;
import com.velora.inventory.dto.ProductRequest;
import com.velora.inventory.dto.ReserveRequest;
import com.velora.inventory.dto.ReserveResponse;
import com.velora.inventory.model.ProductVariant;
import com.velora.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/api/products")
    public ApiResponse<List<ProductVariant>> getProducts() {
        return ApiResponse.success(200, "Products retrieved successfully", inventoryService.getAllProducts());
    }

    @GetMapping("/api/variants/{sku}")
    public ApiResponse<ProductVariant> getVariant(@PathVariable String sku) {
        return ApiResponse.success(200, "Variant retrieved successfully", inventoryService.getVariantBySku(sku));
    }

    @PostMapping("/api/products")
    public ResponseEntity<ApiResponse<ProductVariant>> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201, "Product created successfully", inventoryService.createProduct(request)));
    }

    @PutMapping("/api/products/{id}")
    public ApiResponse<ProductVariant> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ApiResponse.success(200, "Product updated successfully", inventoryService.updateProduct(id, request));
    }

    @DeleteMapping("/api/products/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        inventoryService.deleteProduct(id);
        return ApiResponse.success(200, "Product deleted successfully", null);
    }

    @PostMapping("/api/inventory/reserve")
    public ApiResponse<ReserveResponse> reserve(@Valid @RequestBody ReserveRequest request) {
        return ApiResponse.success(200, "Stock reserved successfully", inventoryService.reserveStock(request));
    }
}
