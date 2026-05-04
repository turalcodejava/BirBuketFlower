package com.birbuket.authservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * true olanda email üçün Kafka producer aktivdir; consumer ayrıca mail servisindədir.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.kafka")
public class AuthKafkaProperties {

    private boolean enabled = false;
    private String emailTopic = "auth.password-reset";
    private String welcomeTopic = "auth.welcome-email";
}
