package com.birbuket.mailservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailServiceDiagnostics implements ApplicationRunner {

    private final MailProperties mailProperties;

    @Override
    public void run(ApplicationArguments args) {
        String user = mailProperties.getUsername();
        String pass = mailProperties.getPassword();
        boolean hasUser = user != null && !user.isBlank();
        boolean hasPass = pass != null && !pass.isBlank();
        log.info(
                "mail-service SMTP: host={}, port={}, userConfigured={}, passwordConfigured={}",
                mailProperties.getHost(),
                mailProperties.getPort(),
                hasUser,
                hasPass);
        if (!hasUser || !hasPass) {
            log.warn(
                    "SMTP üçün MAIL_USERNAME və MAIL_PASSWORD boşdur — Kafka-dan göndərə bilməyəcəksiz. "
                            + "`gradlew :mail-service:bootRun` ilə kökdə `.env`, və ya IntelliJ-da bu env-ləri yazın.");
        }
    }
}
