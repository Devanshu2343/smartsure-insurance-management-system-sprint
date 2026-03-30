package com.insurance.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gateway")
                        .version("1.0")
                        .description("Gateway routes and JWT validation for Insurance Management System")
                        .contact(new Contact()
                                .name("Insurance Platform Team")
                                .email("platform@insurance.local")));
    }
}
