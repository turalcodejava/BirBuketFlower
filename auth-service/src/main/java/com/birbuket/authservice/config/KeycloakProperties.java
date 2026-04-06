package com.birbuket.authservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Keycloak realm və client — token üçün password grant.
 * Client-də "Direct access grants" aktiv olmalıdır.
 */
@Data
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    private String serverUrl = "http://localhost:8080";
    private String realm = "birbuket-realm";
    private String clientId = "auth-service";
    /** Confidential client üçün; public client-də boş ola bilər */
    private String clientSecret = "";

    /**
     * Admin API (POST /admin/realms/.../users) üçün client_credentials.
     * Boşdursa {@code client-id} / {@code client-secret} istifadə olunur (eyni client service account + manage-users olmalıdır).
     */
    private String adminClientId = "";
    private String adminClientSecret = "";

    /**
     * false olanda yalnız DB-yə yazılır (Keycloak admin qurulmayıbsa).
     * true + client-secret boşdursa sinxron avtomatik ötürülür (register işləsin deyə).
     */
    private boolean registerSyncEnabled = true;
}
