import os
import re

def fix_service(service_path):
    yml_path = os.path.join(service_path, 'src/main/resources/application.yml')
    if not os.path.exists(yml_path): return
    
    with open(yml_path, 'r') as f:
        content = f.read()
        
    # Replace JsonDeserializer with ByteArrayDeserializer
    content = content.replace('org.springframework.kafka.support.serializer.JsonDeserializer', 'org.apache.kafka.common.serialization.ByteArrayDeserializer')
    
    with open(yml_path, 'w') as f:
        f.write(content)
        
    config_path = os.path.join(service_path, 'src/main/java/com/example', service_path.split('/')[-1].replace('-', ''), 'config/KafkaConfig.java')
    if os.path.exists(config_path):
        with open(config_path, 'r') as f:
            java_content = f.read()
            
        if 'ByteArrayJsonMessageConverter' not in java_content:
            bean_code = """
    @org.springframework.context.annotation.Bean
    public org.springframework.kafka.support.converter.RecordMessageConverter converter() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter(mapper);
    }
}"""
            java_content = java_content.replace('}\n', bean_code)
            with open(config_path, 'w') as f:
                f.write(java_content)

services = ['inventory-service', 'order-service', 'payment-service', 'shipping-service', 'notification-service']
for s in services:
    fix_service(s)
