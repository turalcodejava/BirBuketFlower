package com.birbuket.plantdoctorservice.mail;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class AgronomistReplyMailSenderTest {

    @Test
    void trySendProbeEmail_worksWithoutSmtpAuth() throws Exception {
        ServerSetup setup = new ServerSetup(0, "127.0.0.1", ServerSetup.PROTOCOL_SMTP);
        GreenMail greenMail = new GreenMail(setup);
        greenMail.start();
        try {
            int port = greenMail.getSmtp().getPort();

            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("localhost");
            mailSender.setPort(port);
            Properties javaMailProps = new Properties();
            javaMailProps.put("mail.transport.protocol", "smtp");
            javaMailProps.put("mail.smtp.auth", "false");
            javaMailProps.put("mail.smtp.starttls.enable", "false");
            mailSender.setJavaMailProperties(javaMailProps);

            MailProperties mailProperties = new MailProperties();
            mailProperties.setHost("localhost");
            mailProperties.setPort(port);
            mailProperties.setUsername("");
            mailProperties.setPassword("");

            AgronomistReplyMailSender sender = new AgronomistReplyMailSender(mailSender, mailProperties);
            ReflectionTestUtils.setField(sender, "fromEmail", "no-reply@birbuket.local");
            ReflectionTestUtils.setField(sender, "gatewayBaseUrl", "http://localhost:8081");
            ReflectionTestUtils.setField(sender, "mailOverrideTo", "");
            ReflectionTestUtils.setField(sender, "mailSmtpAuth", false);

            assertThat(sender.isOutgoingMailConfigured()).isTrue();

            String err = sender.trySendProbeEmail("customer@example.test");
            assertThat(err).isNull();

            assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();
            var received = greenMail.getReceivedMessages()[0];
            assertThat(received.getSubject()).contains("SMTP test");
        } finally {
            greenMail.stop();
        }
    }
}
