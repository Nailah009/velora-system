package com.example.paymentservice.consumer;

import com.example.paymentservice.config.RabbitConfig;
import com.example.paymentservice.event.OrderCreatedEvent;
import com.example.paymentservice.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedConsumer.class);

    private final PaymentService paymentService;

    public OrderCreatedConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @RabbitListener(queues = RabbitConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("""
============================================================
PAYMENT CONSUMER - RECEIVE ORDER CREATED
============================================================
Queue         : {}
Order ID      : {}
Customer      : {}
Amount        : {}
============================================================
""",
                RabbitConfig.ORDER_CREATED_QUEUE,
                event.getOrderId(),
                event.getCustomerName(),
                event.getTotalAmount());
        paymentService.processOrderPayment(event);
    }
}
