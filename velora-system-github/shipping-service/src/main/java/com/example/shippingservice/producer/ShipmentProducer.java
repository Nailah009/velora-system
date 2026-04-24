package com.example.shippingservice.producer;

import com.example.shippingservice.config.KafkaConfig;
import com.example.shippingservice.event.ShipmentCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ShipmentProducer {

    private static final Logger log = LoggerFactory.getLogger(ShipmentProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ShipmentProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendShipmentCreated(ShipmentCreatedEvent event) {
        kafkaTemplate.send(KafkaConfig.SHIPMENT_CREATED_TOPIC, event);

        log.info("""
============================================================
SHIPPING SERVICE - SHIPMENT EVENT PUBLISHED
============================================================
Exchange      : {}
Routing Key   : {}
Order ID      : {}
Tracking No   : {}
============================================================
""",
                KafkaConfig.SHIPMENT_CREATED_TOPIC,
                "",
                event.getOrderId(),
                event.getTrackingNumber());
    }
}