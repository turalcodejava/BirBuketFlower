package com.birbuket.gateway.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // Auth Service üçün
    @Bean
    public GroupedOpenApi authServiceOpenApi() {
        return GroupedOpenApi.builder()
                .group("auth-service")
                .pathsToMatch("/auth/**")   // Gateway routing path
                .build();
    }

    // Product Service üçün
    @Bean
    public GroupedOpenApi productServiceOpenApi() {
        return GroupedOpenApi.builder()
                .group("product-service")
                .pathsToMatch("/product/**")  // Gateway routing path
                .build();
    }
}