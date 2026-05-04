package com.birbuket.authservice.service;

import com.birbuket.authservice.config.KeycloakProperties;
import com.birbuket.common.enums.ErrorCode;
import com.birbuket.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakTokenService {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final KeycloakProperties keycloakProperties;
    private final RestTemplate restTemplate;

    public KeycloakTokenResult obtainPasswordGrant(String username, String password) {
        String base = keycloakProperties.getServerUrl().replaceAll("/$", "");
        String url = base + "/realms/" + keycloakProperties.getRealm()
                + "/protocol/openid-connect/token";

        String secret = keycloakProperties.getClientSecret();
        boolean hasSecret = StringUtils.hasText(secret);
        boolean triedBodySecret = false;

        try {
            try {
                return executePasswordGrant(url, username, password, true);
            } catch (HttpClientErrorException e) {
                // Some Keycloak client auth modes require client_secret in form body instead of basic auth.
                if (hasSecret && e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    log.warn("Keycloak token 401 with basic auth. Falling back to client_secret in request body.");
                    triedBodySecret = true;
                    return executePasswordGrant(url, username, password, false);
                }
                throw e;
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    String body = e.getResponseBodyAsString();
                    if (body == null || body.isBlank()) {
                        log.warn(
                                "Keycloak token 401, boş gövdə — URL={}, realm={}, client-id={}, client_secret göndərilib={}, body fallback tried={}",
                                url,
                                keycloakProperties.getRealm(),
                                keycloakProperties.getClientId(),
                                hasSecret,
                                triedBodySecret);
                    }
                }
                throw new BaseException(
                        "İstifadəçi adı və ya şifrə yanlışdır.",
                        HttpStatus.UNAUTHORIZED,
                        ErrorCode.UNAUTHORIZED);
            }
            throw new BaseException(
                    "Keycloak giriş rədd edildi: " + keycloakErrorDetail(e),
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED);
        } catch (ResourceAccessException e) {
            throw new BaseException(
                    "Keycloak əlçatan deyil: " + e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE,
                    ErrorCode.UNAUTHORIZED);
        } catch (RestClientException e) {
            throw new BaseException(
                    "Keycloak sorğusu uğursuz: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY,
                    ErrorCode.UNAUTHORIZED);
        }
    }

    private KeycloakTokenResult executePasswordGrant(String url, String username, String password, boolean useBasicAuth) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", keycloakProperties.getClientId());
        form.add("scope", "openid profile email phone");
        form.add("username", username);
        form.add("password", password);

        String secret = keycloakProperties.getClientSecret();
        boolean hasSecret = StringUtils.hasText(secret);
        if (hasSecret && !useBasicAuth) {
            form.add("client_secret", secret);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        if (hasSecret && useBasicAuth) {
            headers.setBasicAuth(keycloakProperties.getClientId(), secret);
        }

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(form, headers),
                Map.class);
        Map<?, ?> body = response.getBody();
        if (body == null) {
            throw new BaseException("Keycloak cavabsı boşdur", HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }
        String access = (String) body.get("access_token");
        String refresh = (String) body.get("refresh_token");
        if (access == null) {
            throw new BaseException("Keycloak access_token qaytarmadı", HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }
        return new KeycloakTokenResult(access, refresh != null ? refresh : "");
    }

    /**
     * Keycloak JSON cavabından error / error_description çıxarır (məs. invalid_grant, unauthorized_client).
     * 401 + boş gövdə: OAuth-da çox vaxt invalid_client (client_id/client_secret uyğunsuzluğu).
     */
    private String keycloakErrorDetail(HttpStatusCodeException e) {
        String raw = e.getResponseBodyAsString();
        String wwwAuth = e.getResponseHeaders() != null
                ? e.getResponseHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)
                : null;

        if (raw == null || raw.isBlank()) {
            return "Giriş rədd edildi (401). Keycloak client_id/client_secret və Direct Access Grants ayarlarını yoxlayın.";
        }
        try {
            JsonNode node = JSON.readTree(raw);
            String desc = node.path("error_description").asText("");
            if (!desc.isEmpty()) {
                return desc;
            }
            String err = node.path("error").asText("");
            if (!err.isEmpty()) {
                return err;
            }
        } catch (Exception ignored) {
            // JSON deyilsə, mətni qısaldıb qaytar
        }
        return raw.length() > 200 ? raw.substring(0, 200) + "…" : raw;
    }

    public record KeycloakTokenResult(String accessToken, String refreshToken) {}
}
