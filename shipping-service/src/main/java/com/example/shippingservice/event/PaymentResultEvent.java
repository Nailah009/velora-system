package com.example.shippingservice.event;

import java.math.BigDecimal;
import java.util.List;

public class PaymentResultEvent {
    private String paymentId;
    private String orderId;
    private List<OrderItemPayload> items;
    private BigDecimal amount;
    private String paymentMethod;
    private String email;
    private String customerName;
    private String paymentStatus;
    private String reason;
    private String shippingAddress;
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public List<OrderItemPayload> getItems() { return items; }
    public void setItems(List<OrderItemPayload> items) { this.items = items; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
}
