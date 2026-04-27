package com.birbuket.authservice.notification;

import com.birbuket.authservice.config.AuthKafkaProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mail servisi bu topic-i dinləyib SMTP ilə göndərir.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaPasswordResetNotificationSender implements PasswordResetNotificationSender {

    public static final String EVENT_TYPE = "PASSWORD_RESET_REQUESTED";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AuthKafkaProperties authKafkaProperties;
    private final ObjectMapper objectMapper;

    @Override
    public void sendPasswordResetLink(String toEmail, String username, String resetUrl) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventType", EVENT_TYPE);
        payload.put("toEmail", toEmail);
        payload.put("username", username);
        payload.put("resetUrl", resetUrl);
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(authKafkaProperties.getEmailTopic(), toEmail, json);
            log.debug("Password reset event sent to topic {}", authKafkaProperties.getEmailTopic());
        } catch (Exception e) {
            log.error("Kafka-ya password reset event göndərilə bilmədi", e);
            throw new IllegalStateException("Email hadisəsi göndərilə bilmədi", e);
        }
    }
}
