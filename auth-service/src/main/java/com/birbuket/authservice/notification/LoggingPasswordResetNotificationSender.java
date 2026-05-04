package com.birbuket.authservice.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * KAFKA_ENABLED false olduqda; mail consumer hazır olana qədər link burada loglanır.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingPasswordResetNotificationSender implements PasswordResetNotificationSender {

    @Override
    public void sendPasswordResetLink(String toEmail, String username, String resetUrl) {
        log.warn(
                "[Password reset] EMAIL GÖNDƏRİLMİR — app.kafka.enabled=false (və ya KAFKA_ENABLED=false). "
                        + "Kafka + mail-service işə salın və ya KAFKA_ENABLED=true. to={} user={} link={}",
                toEmail,
                username,
                resetUrl);
    }
}
