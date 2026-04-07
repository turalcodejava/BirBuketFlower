package com.birbuket.authservice;

import com.birbuket.authservice.config.KeycloakProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "com.birbuket")
@EnableConfigurationProperties(KeycloakProperties.class)
@EnableJpaAuditing
@ComponentScan(basePackages = {
        "com.birbuket.authservice",
        "com.birbuket.common"
})
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }


}
