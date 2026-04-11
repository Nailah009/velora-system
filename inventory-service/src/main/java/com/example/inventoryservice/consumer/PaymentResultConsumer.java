package com.example.inventoryservice.consumer;

import com.example.inventoryservice.config.RabbitConfig;
import com.example.inventoryservice.event.PaymentResultEvent;
import com.example.inventoryservice.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentResultConsumer.class);

    private final InventoryService inventoryService;

    public PaymentResultConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @RabbitListener(queues = RabbitConfig.PAYMENT_SUCCESS_QUEUE)
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
                RabbitConfig.PAYMENT_SUCCESS_QUEUE,
                event.getOrderId(),
                event.getCustomerName(),
                event.getPaymentStatus());
        inventoryService.confirmStock(event);
    }

    @RabbitListener(queues = RabbitConfig.PAYMENT_FAILED_QUEUE)
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
                RabbitConfig.PAYMENT_FAILED_QUEUE,
                event.getOrderId(),
                event.getCustomerName(),
                event.getPaymentStatus(),
                event.getReason());
        inventoryService.releaseStock(event);
    }
}
