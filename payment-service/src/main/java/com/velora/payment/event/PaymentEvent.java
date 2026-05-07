package com.velora.payment.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class PaymentEvent {
    public String eventId;
    public Instant occurredAt;
    public String correlationId;
    public String sagaId;
    public Long orderId;
    public Long paymentId;
    public BigDecimal amount;
    public String status;
    public String paymentMethod;
    public String username;
    public String email;
    public String shippingAddress;
    public String reason;
    public String itemsSummary;
    public List<Item> items;

    public static class Item {
        public String sku;
        public String productName;
        public Integer quantity;
        public BigDecimal price;
        public BigDecimal subtotal;
    }
}
