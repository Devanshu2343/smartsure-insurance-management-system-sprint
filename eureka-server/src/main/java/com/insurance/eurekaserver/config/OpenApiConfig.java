package com.insurance.eurekaserver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eurekaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Eureka Server API")
                        .version("1.0")
                        .description("Service discovery API for Insurance Management System")
                        .contact(new Contact()
                                .name("Insurance Platform Team")
                                .email("platform@insurance.local")));
    }
}
