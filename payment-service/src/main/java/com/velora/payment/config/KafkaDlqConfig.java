package com.velora.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaDlqConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${app.kafka.topics.order-dlq:velora.order.topic.DLQ}")
    private String orderDlqTopic;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.deserializer", StringDeserializer.class);
        props.put("value.deserializer", StringDeserializer.class);
        props.put("auto.offset.reset", "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(record.topic() + ".DLQ", record.partition())
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(2000L, 2L)
        );

        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public NewTopic orderDlqNewTopic() {
        return new NewTopic(orderDlqTopic, 1, (short) 1);
    }
}
