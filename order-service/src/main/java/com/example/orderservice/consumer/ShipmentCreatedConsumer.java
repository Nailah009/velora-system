package com.example.orderservice.consumer;

import com.example.orderservice.config.RabbitConfig;
import com.example.orderservice.event.ShipmentCreatedEvent;
import com.example.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ShipmentCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(ShipmentCreatedConsumer.class);

    private final OrderService orderService;

    public ShipmentCreatedConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = RabbitConfig.ORDER_SHIPMENT_CREATED_QUEUE)
    public void handleShipmentCreated(ShipmentCreatedEvent event) {
        log.info("""
============================================================
ORDER CONSUMER - RECEIVE SHIPMENT CREATED
============================================================
Queue         : {}
Order ID      : {}
Tracking No   : {}
Shipment Stat.: {}
============================================================
""",
                RabbitConfig.ORDER_SHIPMENT_CREATED_QUEUE,
                event.getOrderId(),
                event.getTrackingNumber(),
                event.getShipmentStatus());
        orderService.updateFromShipmentCreated(event);
    }
}
