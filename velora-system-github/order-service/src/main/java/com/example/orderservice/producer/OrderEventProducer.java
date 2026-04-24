package com.example.orderservice.producer;

import com.example.orderservice.config.KafkaConfig;
import com.example.orderservice.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderCreated(OrderCreatedEvent event) {
        kafkaTemplate.send(
                KafkaConfig.ORDER_TOPIC, event
        );

        log.info("""
============================================================
ORDER SERVICE - EVENT PUBLISHED
============================================================
Exchange      : {}
Routing Key   : {}
Order ID      : {}
Customer      : {}
============================================================
""",
                KafkaConfig.ORDER_TOPIC,
                "",
                event.getOrderId(),
                event.getCustomerName());
    }
}