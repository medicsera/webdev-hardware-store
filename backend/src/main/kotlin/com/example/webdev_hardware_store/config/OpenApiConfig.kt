package com.example.webdev_hardware_store.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openApi(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Hardware Store API")
                .version("1.0")
                .description("REST API интернет-магазина строительных товаров")
        )
        // Swagger UI будет слать запросы через /api/ — nginx проксирует на бэкенд
        .addServersItem(Server().url("/api").description("Через nginx (docker-compose)"))
        .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
        .components(
            Components().addSecuritySchemes(
                "bearerAuth",
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT-токен из /auth/login или /auth/register")
            )
        )
}
