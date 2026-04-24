package com.velora.payment.event;

import java.math.BigDecimal;
import java.time.Instant;

public class PaymentEvent {
    public String eventId;
    public Instant occurredAt;
    public String correlationId;
    public Long orderId;
    public Long paymentId;
    public BigDecimal amount;
    public String status;
    public String paymentMethod;
    public String username;
    public String email;
    public String shippingAddress;
    public String itemsSummary;
}
