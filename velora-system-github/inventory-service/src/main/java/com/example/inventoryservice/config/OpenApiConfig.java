package com.example.inventoryservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Inventory Service API",
        version = "1.0.0",
        description = "REST API untuk melihat produk, varian, dan reservasi stok pada Velora.",
        contact = @Contact(name = "Velora Team")
    ),
    servers = {
        @Server(url = "http://localhost:8082", description = "Local Server")
    }
)
public class OpenApiConfig {
}