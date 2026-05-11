package com.velora.order.model;

public enum OrderStatus {
    PENDING_INVENTORY,
    INVENTORY_RESERVED,
    INVENTORY_FAILED,
    PENDING_PAYMENT,
    PAID,
    PAYMENT_FAILED,
    READY_TO_SHIP,
    CANCELLED
}
