package com.velora.shipping.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class ShipmentEvent {
    public String eventId;
    public Instant occurredAt;
    public String correlationId;
    public String sagaId;
    public Long orderId;
    public Long shipmentId;
    public String username;
    public String email;
    public String address;
    public String courier;
    public String trackingNumber;
    public String status;
    public String reason;
    public List<Item> items;

    public static class Item {
        public String sku;
        public String productName;
        public Integer quantity;
        public BigDecimal price;
        public BigDecimal subtotal;
    }
}
