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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaWelcomeNotificationSender implements WelcomeNotificationSender {

    public static final String EVENT_TYPE = "WELCOME_EMAIL";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AuthKafkaProperties authKafkaProperties;
    private final ObjectMapper objectMapper;

    @Override
    public void sendWelcomeEmail(String toEmail, String username, String fullName) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventType", EVENT_TYPE);
        payload.put("toEmail", toEmail);
        payload.put("username", username);
        payload.put("fullName", fullName);
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(authKafkaProperties.getWelcomeTopic(), toEmail, json).get(25, TimeUnit.SECONDS);
            log.info("Welcome Kafka topic təsdiq={} alıcı={}", authKafkaProperties.getWelcomeTopic(), toEmail);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Welcome Kafka göndərişində interrupt topic={}", authKafkaProperties.getWelcomeTopic(), ie);
            throw new IllegalStateException("Welcome email hadisəsi yazılmadı", ie);
        } catch (TimeoutException | ExecutionException e) {
            log.error(
                    "Kafka-ya welcome hadisəsində xəta topic={}. Broker işləyir?",
                    authKafkaProperties.getWelcomeTopic(),
                    e);
            throw new IllegalStateException("Welcome email hadisəsi Kafka-ya yazılmadı", e);
        } catch (Exception e) {
            log.error("Kafka-ya welcome email event gonderile bilmedi", e);
            throw new IllegalStateException("Welcome email hadisesi gonderile bilmedi", e);
        }
    }
}
