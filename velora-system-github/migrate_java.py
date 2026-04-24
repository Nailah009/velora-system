import os
import re

def migrate_java_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
    original = content

    # Imports
    content = re.sub(r'import org\.springframework\.amqp\..*?;', '', content)
    
    if '@RabbitListener' in content or 'RabbitTemplate' in content or 'Amqp' in content or 'RabbitConfig' in content:
        if 'import org.springframework.kafka.core.KafkaTemplate;' not in content and 'KafkaTemplate' in content:
            pass # we'll add if needed
        content = content.replace('import org.springframework.amqp.rabbit.core.RabbitTemplate;', 'import org.springframework.kafka.core.KafkaTemplate;')
        content = content.replace('import org.springframework.amqp.rabbit.annotation.RabbitListener;', 'import org.springframework.kafka.annotation.KafkaListener;')
        
        # Replace RabbitTemplate -> KafkaTemplate
        content = content.replace('RabbitTemplate', 'KafkaTemplate<String, Object>')
        # The constructor parameter might be KafkaTemplate<String, Object> rabbitTemplate
        # Let's replace rabbitTemplate with kafkaTemplate
        content = content.replace('rabbitTemplate', 'kafkaTemplate')
        
        # Replace throw AmqpRejectAndDontRequeueException
        content = content.replace('AmqpRejectAndDontRequeueException', 'RuntimeException')
        
        # convertAndSend(exchange, routingKey, event) -> send(topic, event)
        # rabbitTemplate.convertAndSend(RabbitConfig.ORDER_EXCHANGE, "", event);
        content = re.sub(r'kafkaTemplate\.convertAndSend\(([^,]+),\s*""\s*,\s*([^)]+)\)', r'kafkaTemplate.send(\1, \2)', content)
        content = re.sub(r'kafkaTemplate\.convertAndSend\(([^,]+),\s*[^,]+,\s*([^)]+)\)', r'kafkaTemplate.send(\1, \2)', content)

        # @RabbitListener(queues = RabbitConfig.ORDER_CREATED_QUEUE)
        # -> @KafkaListener(topics = KafkaConfig.ORDER_TOPIC, groupId = "${spring.application.name}")
        def listener_repl(match):
            queue_str = match.group(1)
            topic_str = queue_str.replace('_QUEUE', '_TOPIC').replace('_EXCHANGE', '_TOPIC')
            # specific mapping
            if 'ORDER_CREATED_QUEUE' in topic_str: topic_str = 'KafkaConfig.ORDER_TOPIC'
            if 'ORDER_PAYMENT_SUCCESS' in topic_str: topic_str = 'KafkaConfig.PAYMENT_SUCCESS_TOPIC'
            if 'ORDER_PAYMENT_FAILED' in topic_str: topic_str = 'KafkaConfig.PAYMENT_FAILED_TOPIC'
            if 'ORDER_SHIPMENT_CREATED' in topic_str: topic_str = 'KafkaConfig.SHIPMENT_CREATED_TOPIC'
            if 'DLQ' in topic_str: topic_str = 'KafkaConfig.DLQ_TOPIC'
            return f'@KafkaListener(topics = {topic_str}, groupId = "${{spring.application.name}}")'

        content = re.sub(r'@RabbitListener\s*\(\s*queues\s*=\s*([^\)]+)\)', listener_repl, content)

        # Replace constants
        content = content.replace('RabbitConfig.ORDER_EXCHANGE', 'KafkaConfig.ORDER_TOPIC')
        content = content.replace('RabbitConfig.PAYMENT_SUCCESS_EXCHANGE', 'KafkaConfig.PAYMENT_SUCCESS_TOPIC')
        content = content.replace('RabbitConfig.PAYMENT_FAILED_EXCHANGE', 'KafkaConfig.PAYMENT_FAILED_TOPIC')
        content = content.replace('RabbitConfig.SHIPMENT_CREATED_EXCHANGE', 'KafkaConfig.SHIPMENT_CREATED_TOPIC')
        content = content.replace('RabbitConfig.ORDER_CREATED_QUEUE', 'KafkaConfig.ORDER_TOPIC')
        content = content.replace('RabbitConfig.ORDER_PAYMENT_SUCCESS_QUEUE', 'KafkaConfig.PAYMENT_SUCCESS_TOPIC')
        content = content.replace('RabbitConfig.ORDER_PAYMENT_FAILED_QUEUE', 'KafkaConfig.PAYMENT_FAILED_TOPIC')
        content = content.replace('RabbitConfig.ORDER_SHIPMENT_CREATED_QUEUE', 'KafkaConfig.SHIPMENT_CREATED_TOPIC')
        content = content.replace('RabbitConfig.ORDER_CREATED_DLQ', 'KafkaConfig.DLQ_TOPIC')
        
        # Replace other RabbitConfig mentions
        content = content.replace('RabbitConfig', 'KafkaConfig')

        # Add imports if necessary
        if 'KafkaTemplate' in content and 'import org.springframework.kafka.core.KafkaTemplate;' not in content:
            content = content.replace('import org.springframework.stereotype.Component;', 'import org.springframework.kafka.core.KafkaTemplate;\nimport org.springframework.stereotype.Component;')
        if '@KafkaListener' in content and 'import org.springframework.kafka.annotation.KafkaListener;' not in content:
            content = content.replace('import org.springframework.stereotype.Component;', 'import org.springframework.kafka.annotation.KafkaListener;\nimport org.springframework.stereotype.Component;')

        # Some consumers import Message
        content = content.replace('import org.springframework.amqp.core.Message;', '')
        content = content.replace('Message message', 'Object message')

    if content != original:
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"Updated {filepath}")

def create_kafka_config(filepath):
    pkg = ""
    with open(filepath, 'r') as f:
        for line in f:
            if line.startswith('package '):
                pkg = line.strip()
                break
    
    content = f"""{pkg}

import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {{
    public static final String ORDER_TOPIC = "velora.order.topic";
    public static final String PAYMENT_SUCCESS_TOPIC = "velora.payment.success.topic";
    public static final String PAYMENT_FAILED_TOPIC = "velora.payment.failed.topic";
    public static final String SHIPMENT_CREATED_TOPIC = "velora.shipment.created.topic";
    public static final String DLQ_TOPIC = "velora.dlq.topic";
}}
"""
    new_path = filepath.replace('RabbitConfig.java', 'KafkaConfig.java')
    with open(new_path, 'w') as f:
        f.write(content)
    os.remove(filepath) 
    print(f"Replaced {filepath} with {new_path}")


for root, dirs, files in os.walk('/Users/lfrd/Documents/velora-system'):
    for file in files:
        if file.endswith('.java'):
            path = os.path.join(root, file)
            if file == 'RabbitConfig.java':
                create_kafka_config(path)
            else:
                migrate_java_file(path)
