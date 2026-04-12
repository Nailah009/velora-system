package com.example.orderservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String ORDER_EXCHANGE = "velora.order.fanout.exchange";
    public static final String PAYMENT_SUCCESS_EXCHANGE = "velora.payment.success.fanout.exchange";
    public static final String PAYMENT_FAILED_EXCHANGE = "velora.payment.failed.fanout.exchange";
    public static final String SHIPMENT_CREATED_EXCHANGE = "velora.shipment.created.fanout.exchange";
    public static final String DLX = "velora.deadletter.exchange";

    public static final String ORDER_CREATED_QUEUE = "payment.order.created.queue";
    public static final String ORDER_CREATED_DLQ = "payment.order.created.dlq";
    public static final String ORDER_CREATED_DLQ_ROUTING_KEY = "payment.order.created.dlq";

    public static final String ORDER_PAYMENT_SUCCESS_QUEUE = "order.payment.success.queue";
    public static final String ORDER_PAYMENT_FAILED_QUEUE = "order.payment.failed.queue";
    public static final String ORDER_SHIPMENT_CREATED_QUEUE = "order.shipment.created.queue";

    @Bean
    public FanoutExchange orderFanoutExchange() {
        return new FanoutExchange(ORDER_EXCHANGE);
    }

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
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", ORDER_CREATED_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue orderCreatedDlq() {
        return QueueBuilder.durable(ORDER_CREATED_DLQ).build();
    }

    @Bean
    public Queue orderPaymentSuccessQueue() {
        return QueueBuilder.durable(ORDER_PAYMENT_SUCCESS_QUEUE).build();
    }

    @Bean
    public Queue orderPaymentFailedQueue() {
        return QueueBuilder.durable(ORDER_PAYMENT_FAILED_QUEUE).build();
    }

    @Bean
    public Queue orderShipmentCreatedQueue() {
        return QueueBuilder.durable(ORDER_SHIPMENT_CREATED_QUEUE).build();
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue())
                .to(orderFanoutExchange());
    }

    @Bean
    public Binding orderCreatedDlqBinding() {
        return BindingBuilder.bind(orderCreatedDlq())
                .to(deadLetterExchange())
                .with(ORDER_CREATED_DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding orderPaymentSuccessBinding() {
        return BindingBuilder.bind(orderPaymentSuccessQueue())
                .to(paymentSuccessFanoutExchange());
    }

    @Bean
    public Binding orderPaymentFailedBinding() {
        return BindingBuilder.bind(orderPaymentFailedQueue())
                .to(paymentFailedFanoutExchange());
    }

    @Bean
    public Binding orderShipmentCreatedBinding() {
        return BindingBuilder.bind(orderShipmentCreatedQueue())
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
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}