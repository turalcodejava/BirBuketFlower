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
        log.info(
                "[Password reset] to={} user={} link={}  (KAFKA açıq olanda bu əvəzinə topic-ə mesaj gedəcək)",
                toEmail,
                username,
                resetUrl);
    }
}
