package com.birbuket.plantdoctorservice.mail;

import com.birbuket.plantdoctorservice.enums.DiagnosisKind;
import com.birbuket.plantdoctorservice.enums.VisitTimeSlot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgronomistReplyMailSender {

    private static final String PLACEHOLDER_FROM = "no-reply@birbuket.local";
    private static final DateTimeFormatter DATE_AZ =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.gateway-base-url:http://localhost:8081}")
    private String gatewayBaseUrl;
    @Value("${MAIL_OVERRIDE_TO:}")
    private String mailOverrideTo;

    /** Gmail və s. üçün true; Mailpit kimi auth-sız relay üçün YAML-da false. */
    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private boolean mailSmtpAuth;

    /** HOME_VISIT: PENDING → RESERVED — istifadəçiyə “ziyarət qəbul olundu” */
    public void sendHomeVisitReservedConfirmation(
            String customerEmail,
            Long homeVisitId,
            LocalDate visitDate,
            VisitTimeSlot visitTimeSlot,
            String addressLine
    ) {
        String segment = "home-visit";
        String link = normalizeBaseUrl(gatewayBaseUrl) + "/api/plantdoctor/" + segment + "/" + homeVisitId;
        String datePart = visitDate != null ? visitDate.format(DATE_AZ) : "(təyin olunacaq)";
        String slotPart = formatSlotAz(visitTimeSlot);
        String addrPart = addressLine != null && !addressLine.isBlank() ? addressLine : "(ünvan üçün hesabınıza baxın)";

        String body =
                "Salam,\n\n"
                        + "Ev ziyarəti müraciətiniz aqronom tərəfindən qəbul olundu (status: RESERVED).\n\n"
                        + "Təxmini tarix: "
                        + datePart
                        + "\nSaat şəddi: "
                        + slotPart
                        + "\nÜnvan: "
                        + addrPart
                        + "\n\n"
                        + "Detallara keçid: "
                        + link
                        + "\n\n"
                        + "Texniki məlumatlar və son təlimatlar yekunlaşdıqda ayrıca məktubla bildiriləcək.\n\n"
                        + "Hörmətlə,\nBirBuket komandası";

        sendPlainText(customerEmail, "Ev ziyarətiniz qəbul olundu", body, logContext("reserved", homeVisitId));
    }

    /** CONSULTATION: PENDING → RESERVED — aqronom qəbul edəndə istifadəçiyə bildiriş */
    public void sendConsultationReservedConfirmation(
            String customerEmail, Long consultationId, String plantType, String symptomsSummary) {
        String link = normalizeBaseUrl(gatewayBaseUrl) + "/api/plantdoctor/consultation/" + consultationId;
        String plantLine = plantType != null && !plantType.isBlank() ? plantType.trim() : "(göstərilməyib)";
        String symPart = truncatePlain(symptomsSummary, 480);

        String body =
                "Salam,\n\n"
                        + "Onlayn bitki müəllimi müraciətiniz aqronom tərəfindən qəbul olundu (status: RESERVED).\n\n"
                        + "Bitki: "
                        + plantLine
                        + "\nSimptomların qısa xülasəsi: "
                        + symPart
                        + "\n\n"
                        + "Detallı keçid: "
                        + link
                        + "\n\n"
                        + "Müraciətinizə yazılı cavab yekunlaşdıqda ayrıca məktubla xəbər veriləcək.\n\n"
                        + "Hörmətlə,\nBirBuket komandası";

        sendPlainText(
                customerEmail,
                "Müraciətiniz qəbul olundu (#" + consultationId + ")",
                body,
                logContext("consultation-reserved", consultationId));
    }

    /** RESERVED → COMPLETED (və konsultasiya): aqronom cavabı istifadəçi e-poçtuna gedir */
    public void send(
            String customerEmail,
            Long recordId,
            DiagnosisKind recordKind,
            String agronomistResponse,
            String plantType
    ) {
        String segment = recordKind == DiagnosisKind.HOME_VISIT ? "home-visit" : "consultation";
        String diagnosisLink =
                normalizeBaseUrl(gatewayBaseUrl) + "/api/plantdoctor/" + segment + "/" + recordId;

        boolean homeDone = recordKind == DiagnosisKind.HOME_VISIT;
        String subject =
                homeDone
                        ? "Ev ziyarəti müraciətiniz yekunlaşdı (#" + recordId + ")"
                        : "Bitki müəllimi müraciətinizə cavab verildi (#" + recordId + ")";

        String intro =
                homeDone
                        ? "Ev ziyarəti müraciətiniz aqronom tərəfindən yekunlaşdırılıb.\n\n"
                        : "Müraciətinizə aqronom tərəfindən cavab verildi.\n\n";

        String metaLines =
                (plantType != null && !plantType.isBlank())
                        ? "Bitki: " + plantType.trim() + "\n"
                                + "Müraciət №: "
                                + recordId
                                + "\n\n"
                        : "Müraciət №: " + recordId + "\n\n";

        String body =
                "Salam,\n\n"
                        + intro
                        + metaLines
                        + "Cavab:\n"
                        + agronomistResponse
                        + "\n\n"
                        + "Detallı baxış: "
                        + diagnosisLink
                        + "\n\n"
                        + "Hörmətlə,\nBirBuket komandası";

        sendPlainText(customerEmail, subject, body, logContext("completed-reply", recordId));
    }

    /** SMTP / From yoxlanışı (startda loq üçün). */
    public boolean isOutgoingMailConfigured() {
        return isSmtpConfigured() && !resolveEffectiveFromAddress().isBlank();
    }

    /**
     * SMTP-ni əsl göndərişlə yoxlayır (test məktubu). Uğursuzluq halında mesaj üçün baxış loqu.
     *
     * @return null uğurlu; əks halda səbəb
     */
    public String trySendProbeEmail(String toEmail) {
        if (toEmail == null || toEmail.isBlank()) {
            return "to boşdur";
        }
        if (!isSmtpConfigured()) {
            return "SMTP konfiqurasiyası kifayət etmir (host və ya AUTH)";
        }
        String effectiveFrom = resolveEffectiveFromAddress();
        if (effectiveFrom.isBlank()) {
            return "From ünvanı təyin olunmayıb (PLANTDOCTOR_MAIL_FROM və ya MAIL_USERNAME)";
        }
        String body =
                "Plant Doctor SMTP müvəffəq yoxlaması.\n"
                        + "Zaman (UTC): "
                        + Instant.now()
                        + "\n";
        try {
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, false, StandardCharsets.UTF_8.name());
            helper.setFrom(effectiveFrom);
            helper.setTo(resolveRecipient(toEmail.trim()));
            helper.setSubject("BirBuket plantdoctor SMTP test");
            helper.setText(body, false);
            mailSender.send(mime);
            log.info(
                    "SMTP probe göndərildi to={}",
                    maskEmail(resolveRecipient(toEmail.trim())));
            return null;
        } catch (Exception e) {
            log.error(
                    "SMTP probe uğursuz host={}, port={}, smtpAuth={}, err={}",
                    mailProperties.getHost(),
                    mailProperties.getPort(),
                    mailSmtpAuth,
                    e.getMessage(),
                    e);
            String msg = e.getMessage();
            return msg != null ? msg : e.getClass().getSimpleName();
        }
    }

    private void sendPlainText(String customerEmail, String subject, String body, String ctx) {
        if (!isSmtpConfigured()) {
            log.warn(
                    "SMTP qurulmayıb — məktub göndərilmədi [{}]. "
                            + "Gmail: MAIL_USERNAME/MAIL_PASSWORD; Mailpit: MAIL_SMTP_AUTH=false və MAIL_HOST.",
                    ctx);
            return;
        }
        String effectiveFrom = resolveEffectiveFromAddress();
        if (effectiveFrom.isBlank()) {
            log.warn("From ünvanı boşdur — məktub göndərilmədi [{}]", ctx);
            return;
        }
        try {
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, false, StandardCharsets.UTF_8.name());
            helper.setFrom(effectiveFrom);
            helper.setTo(resolveRecipient(customerEmail));
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(mime);
            log.info("Plant doctor email sent [{}] to={}", ctx, maskEmail(resolveRecipient(customerEmail)));
        } catch (Exception e) {
            log.error(
                    "SMTP xətası [{}] host={}, user={}, to(original)={}",
                    ctx,
                    mailProperties.getHost(),
                    maskEmail(mailProperties.getUsername()),
                    customerEmail,
                    e);
        }
    }

    private static String logContext(String type, Long id) {
        return type + ",id=" + id;
    }

    private static String truncatePlain(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        String t = text.trim().replaceAll("\\s+", " ");
        if (t.length() <= maxLen) {
            return t;
        }
        return t.substring(0, maxLen) + "…";
    }

    private static String formatSlotAz(VisitTimeSlot slot) {
        if (slot == null) {
            return "(təyin olunacaq)";
        }
        return switch (slot) {
            case SLOT_09_12 -> "09:00–12:00";
            case SLOT_12_15 -> "12:00–15:00";
            case SLOT_15_18 -> "15:00–18:00";
            case SLOT_18_21 -> "18:00–21:00";
        };
    }

    private boolean isSmtpConfigured() {
        String host = mailProperties.getHost();
        if (host == null || host.isBlank()) {
            return false;
        }
        if (!mailSmtpAuth) {
            return true;
        }
        String user = mailProperties.getUsername();
        String pass = mailProperties.getPassword();
        return user != null && !user.isBlank() && pass != null && !pass.isBlank();
    }

    private String resolveEffectiveFromAddress() {
        String configured = fromEmail != null ? fromEmail.trim() : "";
        boolean explicitFrom =
                !configured.isEmpty() && !PLACEHOLDER_FROM.equalsIgnoreCase(configured);
        if (explicitFrom) {
            return configured;
        }
        String user = mailProperties.getUsername();
        if (user != null && !user.isBlank()) {
            return user.trim();
        }
        if (!mailSmtpAuth) {
            return PLACEHOLDER_FROM;
        }
        return "";
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private String resolveRecipient(String originalTo) {
        String override = mailOverrideTo == null ? "" : mailOverrideTo.trim();
        return override.isBlank() ? originalTo : override;
    }

    private static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "(boş)";
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***" + email.substring(Math.max(at, 0));
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
