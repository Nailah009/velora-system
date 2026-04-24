package com.velora.shipping.event;

import java.time.Instant;

public class ShipmentEvent {
    public String eventId;
    public Instant occurredAt;
    public String correlationId;
    public Long orderId;
    public Long shipmentId;
    public String username;
    public String email;
    public String address;
    public String courier;
    public String trackingNumber;
    public String status;
}
