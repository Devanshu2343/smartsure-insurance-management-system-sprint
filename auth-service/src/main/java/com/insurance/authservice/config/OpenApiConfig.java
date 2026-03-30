package com.insurance.authservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${api.gateway.url:http://localhost:8080}")
    private String gatewayUrl;

    @Bean
    public OpenAPI authServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service API")
                        .version("1.0")
                        .description("Authentication API for Insurance Management System")
                        .contact(new Contact()
                                .name("Insurance Platform Team")
                                .email("devanshu2343@gmail.com")))
                .servers(List.of(new Server().url(gatewayUrl)))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));

    }
}
