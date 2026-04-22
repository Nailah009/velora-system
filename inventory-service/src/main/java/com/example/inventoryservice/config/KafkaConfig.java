package com.example.inventoryservice.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    public static final String ORDER_TOPIC = "velora.order.topic";
    public static final String PAYMENT_SUCCESS_TOPIC = "velora.payment.success.topic";
    public static final String PAYMENT_FAILED_TOPIC = "velora.payment.failed.topic";
    public static final String SHIPMENT_CREATED_TOPIC = "velora.shipment.created.topic";
    public static final String DLQ_TOPIC = "velora.dlq.topic";

    @org.springframework.context.annotation.Bean
    public org.springframework.kafka.support.converter.RecordMessageConverter converter() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter(mapper);
    }
}