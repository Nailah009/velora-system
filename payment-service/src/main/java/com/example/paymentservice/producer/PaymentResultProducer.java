package com.example.paymentservice.producer;

import com.example.paymentservice.config.RabbitConfig;
import com.example.paymentservice.event.PaymentResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentResultProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public PaymentResultProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendSuccess(PaymentResultEvent event) {
        rabbitTemplate.convertAndSend(RabbitConfig.PAYMENT_SUCCESS_EXCHANGE, "", event);
        log.info("""
============================================================
PAYMENT SERVICE - PAYMENT SUCCESS PUBLISHED
============================================================
Exchange      : {}
Routing Key   : <fanout>
Order ID      : {}
============================================================
""",
                RabbitConfig.PAYMENT_SUCCESS_EXCHANGE,
                event.getOrderId());
    }

    public void sendFailure(PaymentResultEvent event) {
        rabbitTemplate.convertAndSend(RabbitConfig.PAYMENT_FAILED_EXCHANGE, "", event);
        log.info("""
============================================================
PAYMENT SERVICE - PAYMENT FAILED PUBLISHED
============================================================
Exchange      : {}
Routing Key   : <fanout>
Order ID      : {}
============================================================
""",
                RabbitConfig.PAYMENT_FAILED_EXCHANGE,
                event.getOrderId());
    }
}