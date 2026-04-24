package com.example.notificationservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Notification Service API",
        version = "1.0.0",
        description = "REST API untuk melihat log notifikasi pada Velora.",
        contact = @Contact(name = "Velora Team")
    ),
    servers = {
        @Server(url = "http://localhost:8085", description = "Local Server")
    }
)
public class OpenApiConfig {
}