package com.velora.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CreateOrderRequest {
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    @Valid
    @NotEmpty(message = "Items cannot be empty")
    private List<OrderItemRequest> items;

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}
