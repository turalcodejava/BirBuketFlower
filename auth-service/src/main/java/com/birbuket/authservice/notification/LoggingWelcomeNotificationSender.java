package com.birbuket.authservice.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingWelcomeNotificationSender implements WelcomeNotificationSender {
    @Override
    public void sendWelcomeEmail(String toEmail, String username, String fullName) {
        log.warn(
                "[Welcome email] EMAIL GÖNDƏRİLMİR — KAFKA_DISABLED. Kafka + mail-service ilə KAFKA_ENABLED=true. "
                        + "to={} username={} fullName={}",
                toEmail,
                username,
                fullName);
    }
}
