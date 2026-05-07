package com.velora.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ReserveItemRequest {
    @NotBlank(message = "SKU is required")
    private String sku;
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
