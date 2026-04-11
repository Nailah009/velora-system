package com.example.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ReserveItemRequest {

    @NotBlank(message = "sku is required")
    private String sku;

    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
