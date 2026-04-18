package com.example.notificationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String PAYMENT_SUCCESS_EXCHANGE = "velora.payment.success.fanout.exchange";
    public static final String PAYMENT_FAILED_EXCHANGE = "velora.payment.failed.fanout.exchange";
    public static final String SHIPMENT_CREATED_EXCHANGE = "velora.shipment.created.fanout.exchange";

    public static final String PAYMENT_SUCCESS_QUEUE = "notification.payment.success.queue";
    public static final String PAYMENT_FAILED_QUEUE = "notification.payment.failed.queue";
    public static final String SHIPMENT_CREATED_QUEUE = "notification.shipment.created.queue";

    @Bean
    public FanoutExchange paymentSuccessFanoutExchange() {
        return new FanoutExchange(PAYMENT_SUCCESS_EXCHANGE);
    }

    @Bean
    public FanoutExchange paymentFailedFanoutExchange() {
        return new FanoutExchange(PAYMENT_FAILED_EXCHANGE);
    }

    @Bean
    public FanoutExchange shipmentCreatedFanoutExchange() {
        return new FanoutExchange(SHIPMENT_CREATED_EXCHANGE);
    }

    @Bean
    public Queue paymentSuccessQueue() {
        return QueueBuilder.durable(PAYMENT_SUCCESS_QUEUE).build();
    }

    @Bean
    public Queue paymentFailedQueue() {
        return QueueBuilder.durable(PAYMENT_FAILED_QUEUE).build();
    }

    @Bean
    public Queue shipmentCreatedQueue() {
        return QueueBuilder.durable(SHIPMENT_CREATED_QUEUE).build();
    }

    @Bean
    public Binding paymentSuccessBinding() {
        return BindingBuilder.bind(paymentSuccessQueue())
                .to(paymentSuccessFanoutExchange());
    }

    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder.bind(paymentFailedQueue())
                .to(paymentFailedFanoutExchange());
    }

    @Bean
    public Binding shipmentCreatedBinding() {
        return BindingBuilder.bind(shipmentCreatedQueue())
                .to(shipmentCreatedFanoutExchange());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}