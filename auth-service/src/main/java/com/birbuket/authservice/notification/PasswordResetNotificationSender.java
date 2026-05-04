package com.birbuket.authservice.notification;

/**
 * Şifrə sıfırlama linki: Kafka (consumer mail göndərər) və ya lokal inkişf üçün log.
 */
public interface PasswordResetNotificationSender {

    void sendPasswordResetLink(String toEmail, String username, String resetUrl);
}
