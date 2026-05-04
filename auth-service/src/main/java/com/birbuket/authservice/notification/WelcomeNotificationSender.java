package com.birbuket.authservice.notification;

public interface WelcomeNotificationSender {
    void sendWelcomeEmail(String toEmail, String username, String fullName);
}
