package com.example.orderservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "velora.topic.exchange";
    public static final String ORDER_CREATED_QUEUE = "payment.order.created.queue";
    public static final String ORDER_PAYMENT_SUCCESS_QUEUE = "order.payment.success.queue";
    public static final String ORDER_PAYMENT_FAILED_QUEUE = "order.payment.failed.queue";
    public static final String ORDER_SHIPMENT_CREATED_QUEUE = "order.shipment.created.queue";

    @Bean
    public TopicExchange exchange() { return new TopicExchange(EXCHANGE); }

    @Bean
    public Queue orderCreatedQueue() { return new Queue(ORDER_CREATED_QUEUE, true); }

    @Bean
    public Queue orderPaymentSuccessQueue() { return new Queue(ORDER_PAYMENT_SUCCESS_QUEUE, true); }

    @Bean
    public Queue orderPaymentFailedQueue() { return new Queue(ORDER_PAYMENT_FAILED_QUEUE, true); }

    @Bean
    public Queue orderShipmentCreatedQueue() { return new Queue(ORDER_SHIPMENT_CREATED_QUEUE, true); }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue()).to(exchange()).with("order.created");
    }

    @Bean
    public Binding orderPaymentSuccessBinding() {
        return BindingBuilder.bind(orderPaymentSuccessQueue()).to(exchange()).with("payment.success");
    }

    @Bean
    public Binding orderPaymentFailedBinding() {
        return BindingBuilder.bind(orderPaymentFailedQueue()).to(exchange()).with("payment.failed");
    }

    @Bean
    public Binding orderShipmentCreatedBinding() {
        return BindingBuilder.bind(orderShipmentCreatedQueue()).to(exchange()).with("shipment.created");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
