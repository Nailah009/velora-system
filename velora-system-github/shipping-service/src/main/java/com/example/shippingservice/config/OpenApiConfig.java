package com.example.shippingservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Shipping Service API",
        version = "1.0.0",
        description = "REST API untuk melihat data pengiriman pada Velora.",
        contact = @Contact(name = "Velora Team")
    ),
    servers = {
        @Server(url = "http://localhost:8081", description = "Local Server")
    }
)
public class OpenApiConfig {
}