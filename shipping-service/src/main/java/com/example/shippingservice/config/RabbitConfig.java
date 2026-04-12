package com.example.shippingservice.config;

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
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String PAYMENT_SUCCESS_EXCHANGE = "velora.payment.success.fanout.exchange";
    public static final String SHIPMENT_CREATED_EXCHANGE = "velora.shipment.created.fanout.exchange";
    public static final String DLX = "velora.deadletter.exchange";

    public static final String PAYMENT_SUCCESS_QUEUE = "shipping.payment.success.queue";
    public static final String PAYMENT_SUCCESS_DLQ = "shipping.payment.success.dlq";
    public static final String PAYMENT_SUCCESS_DLQ_ROUTING_KEY = "shipping.payment.success.dlq";

    @Bean
    public FanoutExchange paymentSuccessFanoutExchange() {
        return new FanoutExchange(PAYMENT_SUCCESS_EXCHANGE);
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
    public Queue paymentSuccessQueue() {
        return QueueBuilder.durable(PAYMENT_SUCCESS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", PAYMENT_SUCCESS_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue paymentSuccessDlq() {
        return QueueBuilder.durable(PAYMENT_SUCCESS_DLQ).build();
    }

    @Bean
    public Binding paymentSuccessBinding() {
        return BindingBuilder.bind(paymentSuccessQueue())
                .to(paymentSuccessFanoutExchange());
    }

    @Bean
    public Binding paymentSuccessDlqBinding() {
        return BindingBuilder.bind(paymentSuccessDlq())
                .to(deadLetterExchange())
                .with(PAYMENT_SUCCESS_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean(name = "rawRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory rawRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new SimpleMessageConverter());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
