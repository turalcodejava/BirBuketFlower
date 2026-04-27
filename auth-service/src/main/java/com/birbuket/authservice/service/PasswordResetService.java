package com.birbuket.authservice.service;

import com.birbuket.authservice.config.PasswordResetProperties;
import com.birbuket.authservice.dto.ForgotPasswordRequest;
import com.birbuket.authservice.dto.ResetPasswordRequest;
import com.birbuket.authservice.models.PasswordResetTokenEntity;
import com.birbuket.authservice.models.UserEntity;
import com.birbuket.authservice.notification.PasswordResetNotificationSender;
import com.birbuket.authservice.repository.PasswordResetTokenRepository;
import com.birbuket.authservice.repository.UserRepository;
import com.birbuket.authservice.util.ResetTokenUtils;
import com.birbuket.common.enums.ErrorCode;
import com.birbuket.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Mənbə: DB (token) + Kafka (link göndərmə) + Keycloak (login parolu).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    public static final String FORGOT_PASSWORD_GENERIC_OK =
            "Əgər bu email qeydiyyatdadırsa, sizə sıfırlama linki göndəriləcək.";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakAdminService keycloakAdminService;
    private final PasswordResetProperties passwordResetProperties;
    private final PasswordResetNotificationSender passwordResetNotificationSender;

    @Transactional
    public void requestForgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        if (email.isEmpty()) {
            return;
        }
        userRepository.findByEmail(email).ifPresent(this::createAndSendReset);
    }

    private void createAndSendReset(UserEntity user) {
        passwordResetTokenRepository.markUnusedTokensUsedForUser(user.getId());

        String raw = ResetTokenUtils.generateRawToken();
        String hash = ResetTokenUtils.sha256Hex(raw);

        Instant exp = Instant.now().plus(
                passwordResetProperties.getTokenTtlMinutes() > 0
                        ? passwordResetProperties.getTokenTtlMinutes()
                        : 60,
                ChronoUnit.MINUTES);

        PasswordResetTokenEntity entity = PasswordResetTokenEntity.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(exp)
                .used(false)
                .build();
        passwordResetTokenRepository.save(entity);

        String resetUrl = buildResetUrl(raw);
        passwordResetNotificationSender.sendPasswordResetLink(user.getEmail(), user.getUsername(), resetUrl);
    }

    private String buildResetUrl(String rawToken) {
        String base = passwordResetProperties.getResetPageBaseUrl();
        if (base == null) {
            base = "http://localhost:3000/reset-password";
        }
        base = base.trim();
        String sep = base.contains("?") ? "&" : "?";
        return base + sep + "token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BaseException("Parollar uyğun gəlmir", HttpStatus.BAD_REQUEST, ErrorCode.PASSWORD_MISMATCH);
        }
        String raw = request.getToken() != null ? request.getToken().trim() : "";
        if (raw.isEmpty()) {
            throw new BaseException(
                    "Sıfırlama linki etibarsızdır və ya vaxtı bitib",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.PASSWORD_RESET_INVALID);
        }
        String hash = ResetTokenUtils.sha256Hex(raw);
        PasswordResetTokenEntity pr = passwordResetTokenRepository
                .findByTokenHashAndUsedIsFalse(hash)
                .orElseThrow(
                        () -> new BaseException(
                                "Sıfırlama linki etibarsızdır və ya artıq istifadə olunub",
                                HttpStatus.BAD_REQUEST,
                                ErrorCode.PASSWORD_RESET_INVALID));

        if (pr.getExpiresAt().isBefore(Instant.now())) {
            throw new BaseException(
                    "Sıfırlama linkinin müddəti bitib",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.PASSWORD_RESET_INVALID);
        }

        UserEntity user = pr.getUser();
        if (user == null) {
            throw new BaseException("İstifadəçi tapılmadı", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND);
        }

        keycloakAdminService
                .findKeycloakUserIdByEmailOrUsername(user.getEmail(), user.getUsername())
                .ifPresentOrElse(
                        kcId -> keycloakAdminService.setUserPasswordInKeycloak(
                                kcId,
                                request.getNewPassword()),
                        () -> log.warn("Keycloak-da user id tapılmadı, yalnız DB parolu yenilənir: username={}", user.getUsername()));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        pr.setUsed(true);
        passwordResetTokenRepository.save(pr);
    }
}
