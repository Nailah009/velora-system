package com.velora.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.velora.order.model.Order;
import com.velora.order.model.OrderItem;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;

public class OrderResponse {
    @JsonProperty("order_id")
    private String orderId;
    @JsonProperty("internal_id")
    private Long internalId;
    private Long userId;
    private String username;
    private String email;
    private String shippingAddress;
    private String paymentMethod;
    private BigDecimal total;
    private String status;
    private String correlationId;
    private String createdAt;
    private String updatedAt;
    private List<ItemResponse> items;

    public static OrderResponse from(Order order) {
        OrderResponse response = new OrderResponse();
        response.orderId = generateOrderNumber(order);
        response.internalId = order.getId();
        response.userId = order.getUserId();
        response.username = order.getUsername();
        response.email = order.getEmail();
        response.shippingAddress = order.getShippingAddress();
        response.paymentMethod = order.getPaymentMethod();
        response.total = order.getTotalAmount();
        response.status = order.getStatus() == null ? null : order.getStatus().name();
        response.correlationId = order.getCorrelationId();
        response.createdAt = order.getCreatedAt() == null ? null : order.getCreatedAt().toString();
        response.updatedAt = order.getUpdatedAt() == null ? null : order.getUpdatedAt().toString();
        response.items = order.getItems().stream().map(ItemResponse::from).toList();
        return response;
    }

    private static String generateOrderNumber(Order order) {
        int year = order.getCreatedAt() == null ? java.time.Year.now().getValue() : order.getCreatedAt().atZone(ZoneId.systemDefault()).getYear();
        return "ORD-" + year + "-" + String.format("%04d", order.getId());
    }

    public static class ItemResponse {
        private Long id;
        private String sku;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;

        public static ItemResponse from(OrderItem item) {
            ItemResponse response = new ItemResponse();
            response.id = item.getId();
            response.sku = item.getSku();
            response.productName = item.getProductName();
            response.quantity = item.getQuantity();
            response.price = item.getPrice();
            response.subtotal = item.getSubtotal();
            return response;
        }
        public Long getId() { return id; }
        public String getSku() { return sku; }
        public String getProductName() { return productName; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getPrice() { return price; }
        public BigDecimal getSubtotal() { return subtotal; }
    }

    public String getOrderId() { return orderId; }
    public Long getInternalId() { return internalId; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getShippingAddress() { return shippingAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public BigDecimal getTotal() { return total; }
    public String getStatus() { return status; }
    public String getCorrelationId() { return correlationId; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public List<ItemResponse> getItems() { return items; }
}
