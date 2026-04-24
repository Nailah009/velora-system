package com.velora.order.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderCreatedEvent {
    public String eventId;
    public Instant occurredAt;
    public String correlationId;
    public Long orderId;
    public Long userId;
    public String username;
    public String email;
    public BigDecimal totalAmount;
    public String paymentMethod;
    public String shippingAddress;
    public List<Item> items;

    public static class Item {
        public String sku;
        public String productName;
        public Integer quantity;
        public BigDecimal price;
        public BigDecimal subtotal;
    }
}
