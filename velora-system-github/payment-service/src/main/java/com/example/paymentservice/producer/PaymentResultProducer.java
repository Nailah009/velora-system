package com.example.paymentservice.producer;

import com.example.paymentservice.config.KafkaConfig;
import com.example.paymentservice.event.PaymentResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentResultProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentResultProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendSuccess(PaymentResultEvent event) {
        kafkaTemplate.send(KafkaConfig.PAYMENT_SUCCESS_TOPIC, event);
        log.info("""
============================================================
PAYMENT SERVICE - PAYMENT SUCCESS PUBLISHED
============================================================
Exchange      : {}
Routing Key   : <fanout>
Order ID      : {}
============================================================
""",
                KafkaConfig.PAYMENT_SUCCESS_TOPIC,
                event.getOrderId());
    }

    public void sendFailure(PaymentResultEvent event) {
        kafkaTemplate.send(KafkaConfig.PAYMENT_FAILED_TOPIC, event);
        log.info("""
============================================================
PAYMENT SERVICE - PAYMENT FAILED PUBLISHED
============================================================
Exchange      : {}
Routing Key   : <fanout>
Order ID      : {}
============================================================
""",
                KafkaConfig.PAYMENT_FAILED_TOPIC,
                event.getOrderId());
    }
}