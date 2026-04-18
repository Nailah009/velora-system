package com.example.inventoryservice.dto;

import java.util.List;

public class InventoryReserveResponse {

    private String orderId;
    private String status;
    private List<ReserveItemRequest> items;

    public InventoryReserveResponse() {
    }

    public InventoryReserveResponse(String orderId, String status, List<ReserveItemRequest> items) {
        this.orderId = orderId;
        this.status = status;
        this.items = items;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<ReserveItemRequest> getItems() { return items; }
    public void setItems(List<ReserveItemRequest> items) { this.items = items; }
}
