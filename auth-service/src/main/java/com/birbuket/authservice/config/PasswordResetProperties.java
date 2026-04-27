package com.birbuket.authservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Frontend səhifə: token query paramı ilə, məs. https://app/reset-password?token=...
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.password-reset")
public class PasswordResetProperties {

    /** Sıfırlama linki üçün TTL */
    private int tokenTtlMinutes = 60;

    /**
     * Sonda query əlavə olunur: ?token=... — əgər sonda ? varsa, &token=... istifadə edilir.
     */
    private String resetPageBaseUrl = "http://localhost:3000/reset-password";
}
