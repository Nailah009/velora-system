package com.example.inventoryservice.consumer;

import com.example.inventoryservice.config.KafkaConfig;
import com.example.inventoryservice.event.PaymentResultEvent;
import com.example.inventoryservice.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentResultConsumer.class);

    private final InventoryService inventoryService;

    public PaymentResultConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = KafkaConfig.PAYMENT_SUCCESS_TOPIC, groupId = "${spring.application.name}")
    public void handlePaymentSuccess(PaymentResultEvent event) {
        log.info("""
============================================================
INVENTORY CONSUMER - RECEIVE PAYMENT SUCCESS
============================================================
Queue         : {}
Order ID      : {}
Customer      : {}
Payment Status: {}
============================================================
""",
                KafkaConfig.PAYMENT_SUCCESS_TOPIC,
                event.getOrderId(),
                event.getCustomerName(),
                event.getPaymentStatus());
        inventoryService.confirmStock(event);
    }

    @KafkaListener(topics = KafkaConfig.PAYMENT_FAILED_TOPIC, groupId = "${spring.application.name}")
    public void handlePaymentFailed(PaymentResultEvent event) {
        log.info("""
============================================================
INVENTORY CONSUMER - RECEIVE PAYMENT FAILED
============================================================
Queue         : {}
Order ID      : {}
Customer      : {}
Payment Status: {}
Reason        : {}
============================================================
""",
                KafkaConfig.PAYMENT_FAILED_TOPIC,
                event.getOrderId(),
                event.getCustomerName(),
                event.getPaymentStatus(),
                event.getReason());
        inventoryService.releaseStock(event);
    }
}
