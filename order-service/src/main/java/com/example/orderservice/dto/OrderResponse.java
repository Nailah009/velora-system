package com.example.orderservice.dto;

import java.math.BigDecimal;

public class OrderResponse {

    private String orderId;
    private String status;
    private BigDecimal totalAmount;

    public OrderResponse() {
    }

    public OrderResponse(String orderId, String status, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}
