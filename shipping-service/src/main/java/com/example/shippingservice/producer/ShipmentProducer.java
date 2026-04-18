package com.example.shippingservice.producer;

import com.example.shippingservice.config.RabbitConfig;
import com.example.shippingservice.event.ShipmentCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ShipmentProducer {

    private static final Logger log = LoggerFactory.getLogger(ShipmentProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public ShipmentProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendShipmentCreated(ShipmentCreatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitConfig.SHIPMENT_CREATED_EXCHANGE, "", event);

        log.info("""
============================================================
SHIPPING SERVICE - EVENT PUBLISHED
============================================================
Exchange      : {}
Routing Key   : <fanout>
Order ID      : {}
Tracking No   : {}
============================================================
""",
                RabbitConfig.SHIPMENT_CREATED_EXCHANGE,
                event.getOrderId(),
                event.getTrackingNumber());
    }
}