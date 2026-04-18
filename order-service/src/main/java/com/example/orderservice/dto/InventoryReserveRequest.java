package com.example.orderservice.dto;

import java.util.List;

public class InventoryReserveRequest {

    private String orderId;
    private List<InventoryReserveItemRequest> items;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public List<InventoryReserveItemRequest> getItems() { return items; }
    public void setItems(List<InventoryReserveItemRequest> items) { this.items = items; }
}
