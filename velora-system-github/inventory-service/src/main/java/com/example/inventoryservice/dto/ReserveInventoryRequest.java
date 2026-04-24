package com.example.inventoryservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ReserveInventoryRequest {

    @NotBlank(message = "orderId is required")
    private String orderId;

    @Valid
    @NotEmpty(message = "items must not be empty")
    private List<ReserveItemRequest> items;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public List<ReserveItemRequest> getItems() { return items; }
    public void setItems(List<ReserveItemRequest> items) { this.items = items; }
}
