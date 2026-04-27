package com.birbuket.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway-də REST controller yoxdur — OpenAPI boş ola bilər.
 * Əsas API sənədləri: auth-service (8082), product-service (8083) üzərindən.
 */
@Configuration
public class SwaggerConfig {
    @Value("${swagger.auth-url:http://localhost:8082/swagger-ui.html}")
    private String authSwaggerUrl;

    @Value("${swagger.product-url:http://localhost:8083/swagger-ui.html}")
    private String productSwaggerUrl;

    @Bean
    public OpenAPI gatewayOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gateway")
                        .version("1.0")
                        .description(
                                "Marşrutlar: /api/auth/** → auth-service, /api/product/** → product-service, /uploads/** → product-service. "
                                        + "Tam Swagger: " + authSwaggerUrl + " (auth), "
                                        + productSwaggerUrl + " (product)"));
    }
}
