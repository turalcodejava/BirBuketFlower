package com.birbuket.plantdoctorservice;

import com.birbuket.plantdoctorservice.mail.AgronomistReplyMailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Socket;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlantDoctorMailDiagnostics implements ApplicationRunner {

    private final MailProperties mailProperties;
    private final AgronomistReplyMailSender agronomistReplyMailSender;
    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        String user = mailProperties.getUsername();
        String pass = mailProperties.getPassword();
        boolean hasUser = user != null && !user.isBlank();
        boolean hasPass = pass != null && !pass.isBlank();

        Boolean smtpAuthBox =
                environment.getProperty("spring.mail.properties.mail.smtp.auth", Boolean.class);
        boolean smtpAuth = !Boolean.FALSE.equals(smtpAuthBox);

        Boolean startTlsEnable =
                environment.getProperty("spring.mail.properties.mail.smtp.starttls.enable", Boolean.class);

        Boolean startTlsRequired =
                environment.getProperty("spring.mail.properties.mail.smtp.starttls.required", Boolean.class);

        log.info(
                "plantdoctor SMTP: host={}, port={}, userConfigured={}, passwordConfigured={}, agronomReplyReady={}, "
                        + "effective smtp.auth={}, starttls.enable={}, starttls.required={}",
                mailProperties.getHost(),
                mailProperties.getPort(),
                hasUser,
                hasPass,
                agronomistReplyMailSender.isOutgoingMailConfigured(),
                smtpAuth,
                Boolean.TRUE.equals(startTlsEnable),
                Boolean.TRUE.equals(startTlsRequired));
        probeSocketReachable();
        if ("localhost".equalsIgnoreCase(mailProperties.getHost())
                || "127.0.0.1".equals(mailProperties.getHost())) {
            log.info(
                    "Mailpit üçün tipik: MAIL_PORT=1025, MAIL_SMTP_AUTH=false, MAIL_STARTTLS_ENABLE=false, "
                            + "`docker compose up mailpit`, UI http://localhost:8025");
        }
        if (!agronomistReplyMailSender.isOutgoingMailConfigured()) {
            log.warn(
                    "Aqronom cavabı istifadəçi e-poçtuna göndərilməyəcək. Yoxlayın: "
                            + "(1) .env yüklənir (kökdən bootRun və ya IntelliJ Working directory). "
                            + "(2) Gmail: MAIL_HOST/USERNAME/PASSWORD və App Password. "
                            + "(3) Mailpit: MAIL_HOST=localhost, MAIL_PORT=1025, AUTH=false. "
                            + "(4) From: PLANTDOCTOR_MAIL_FROM və ya MAIL_USERNAME.");
        }
    }

    private void probeSocketReachable() {
        String host = mailProperties.getHost();
        Integer portObj = mailProperties.getPort();
        if (host == null || host.isBlank() || portObj == null) {
            log.warn("plantdoctor SMTP host/port təyin olunmayıb");
            return;
        }
        int port = portObj;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 2500);
            log.info("plantdoctor SMTP port açıqdır {}:{}", host, port);
        } catch (Exception e) {
            log.warn(
                    "plantdoctor SMTP-yə TCP qoşulması mümkün olmadı {}:{} — prosesdə xəta gözləyin və ya Mailpit işlədir? ({})",
                    host,
                    port,
                    e.getMessage());
        }
    }
}
