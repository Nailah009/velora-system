package com.example.orderservice.producer;

import com.example.orderservice.config.RabbitConfig;
import com.example.orderservice.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public OrderEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendOrderCreated(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "order.created", event);
        log.info("""
============================================================
ORDER SERVICE - EVENT PUBLISHED
============================================================
Exchange      : {}
Routing Key   : order.created
Order ID      : {}
Customer      : {}
============================================================
""",
                RabbitConfig.EXCHANGE,
                event.getOrderId(),
                event.getCustomerName());
    }
}
