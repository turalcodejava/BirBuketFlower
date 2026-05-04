package com.birbuket.mailservice.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordResetEmailConsumer {

    private final ObjectMapper objectMapper;
    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    @Value("${MAIL_OVERRIDE_TO:}")
    private String mailOverrideTo;
    @Value("${MAIL_FORCE_FROM:${MAIL_USERNAME:}}")
    private String mailForceFrom;

    @KafkaListener(
            topics = "${app.kafka.password-reset-topic:auth.password-reset}",
            groupId = "${spring.kafka.consumer.group-id:mail-service-group}")
    public void consumePasswordReset(String message) {
        log.info("Kafka password-reset sətir alındı (topic consumer), uzunluq={}", message != null ? message.length() : 0);
        try {
            JsonNode node = objectMapper.readTree(message);
            String toEmail = node.path("toEmail").asText("");
            String username = node.path("username").asText("");
            String resetUrl = node.path("resetUrl").asText("");

            if (toEmail.isBlank() || resetUrl.isBlank()) {
                log.warn("Password reset event skipped: invalid payload {}", message);
                return;
            }

            String recipient = resolveRecipient(toEmail);
            sendMime(recipient, "Sifreni yenile", buildBody(username, resetUrl));
            log.info("Password reset email sent to {} (original={})", recipient, toEmail);
        } catch (Exception e) {
            log.error("Failed to process password reset event: {}", message, e);
        }
    }

    @KafkaListener(
            topics = "${app.kafka.welcome-topic:auth.welcome-email}",
            groupId = "${spring.kafka.consumer.group-id:mail-service-group}")
    public void consumeWelcome(String message) {
        log.info("Kafka welcome-email sətir alındı, uzunluq={}", message != null ? message.length() : 0);
        try {
            JsonNode node = objectMapper.readTree(message);
            String toEmail = node.path("toEmail").asText("");
            String username = node.path("username").asText("");
            String fullName = node.path("fullName").asText("");

            if (toEmail.isBlank()) {
                log.warn("Welcome event skipped: invalid payload {}", message);
                return;
            }

            String displayName = !fullName.isBlank() ? fullName : (username.isBlank() ? "istifadeci" : username);
            String recipient = resolveRecipient(toEmail);
            sendMime(
                    recipient,
                    "Xos geldiniz!",
                    "Salam " + displayName + ",\n\n"
                            + "Qeydiyyat ugurla tamamlandi. BirBuket ailesine xos geldiniz!\n\n"
                            + "Tesekkurler.");
            log.info("Welcome email sent to {} (original={})", recipient, toEmail);
        } catch (Exception e) {
            log.error("Failed to process welcome email event: {}", message, e);
        }
    }

    private void sendMime(String to, String subject, String textPlain) throws MessagingException {
        var mime = mailSender.createMimeMessage();
        var h = new MimeMessageHelper(mime, false, StandardCharsets.UTF_8.name());
        String from = (mailForceFrom == null || mailForceFrom.isBlank())
                ? mailProperties.getUsername()
                : mailForceFrom;
        if (from != null && !from.isBlank()) {
            h.setFrom(from.trim());
        }
        h.setTo(to);
        h.setSubject(subject);
        h.setText(textPlain, false);
        mailSender.send(mime);
    }

    private String resolveRecipient(String originalTo) {
        String override = mailOverrideTo == null ? "" : mailOverrideTo.trim();
        return override.isBlank() ? originalTo : override;
    }

    private String buildBody(String username, String resetUrl) {
        String safeUsername = username == null || username.isBlank() ? "istifadeci" : username;
        return "Salam " + safeUsername + ",\n\n"
                + "Sifreni yenilemek ucun asagidaki linke daxil ol:\n"
                + resetUrl + "\n\n"
                + "Eger bu sorunu siz etmeyibsinizse, bu mesaji ignore edin.";
    }
}
