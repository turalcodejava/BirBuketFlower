package com.birbuket.authservice.service;

import com.birbuket.authservice.config.KeycloakProperties;
import com.birbuket.common.enums.ErrorCode;
import com.birbuket.common.exception.BaseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Qeydiyyat zamanı Keycloak-da istifadəçi yaradır.
 * Keycloak-da client: Service accounts ON, realm-management → manage-users (və query-users) təyin olunmalıdır.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminService {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final KeycloakProperties keycloakProperties;
    private final RestTemplate restTemplate;

    /**
     * Keycloak-da user yaratmaq üçün lazımi credential və konfiq varmı.
     */
    public boolean shouldProvision() {
        return provisionSkipReason().isEmpty();
    }

    /**
     * Sinxron ötürülərsə səbəb (məs. loq üçün); boşdursa sinxron işləyəcək.
     */
    public Optional<String> provisionSkipReason() {
        if (!keycloakProperties.isRegisterSyncEnabled()) {
            return Optional.of("keycloak.register-sync-enabled=false");
        }
        if (resolveServiceAccountSecret().isBlank()) {
            return Optional.of(
                    "KEYCLOAK_CLIENT_SECRET və ya keycloak.admin-client-secret təyin olunmayıb (boşdur)");
        }
        return Optional.empty();
    }

    public void createUser(
            String username,
            String email,
            String firstName,
            String lastName,
            String phoneNumber,
            LocalDate birthDate,
            String rawPassword) {
        String token = obtainServiceAccountToken();
        String base = keycloakProperties.getServerUrl().replaceAll("/$", "");
        String url = base + "/admin/realms/" + keycloakProperties.getRealm() + "/users";

        Map<String, Object> credential = new LinkedHashMap<>();
        credential.put("type", "password");
        credential.put("value", rawPassword);
        credential.put("temporary", false);
        List<Map<String, Object>> credentials = new ArrayList<>();
        credentials.add(credential);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", username);
        body.put("enabled", true);
        body.put("email", email);
        body.put("emailVerified", true);
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        Map<String, List<String>> attributes = new LinkedHashMap<>();
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            attributes.put("phoneNumber", List.of(phoneNumber));
        }
        if (birthDate != null) {
            attributes.put("birthDate", List.of(birthDate.toString()));
        }
        if (!attributes.isEmpty()) {
            body.put("attributes", attributes);
        }
        body.put("credentials", credentials);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String json = JSON.writeValueAsString(body);
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(json, headers),
                    Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BaseException(
                        "Keycloak istifadəçi yaradıla bilmədi: " + response.getStatusCode(),
                        HttpStatus.BAD_GATEWAY,
                        ErrorCode.KEYCLOAK_PROVISIONING_FAILED);
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new BaseException(
                        "Bu istifadəçi adı və ya email Keycloak-da artıq mövcuddur",
                        HttpStatus.CONFLICT,
                        ErrorCode.USER_ALREADY_EXISTS);
            }
            throw new BaseException(
                    "Keycloak istifadəçi yaradılmadı: " + keycloakErrorDetail(e),
                    HttpStatus.BAD_GATEWAY,
                    ErrorCode.KEYCLOAK_PROVISIONING_FAILED);
        } catch (HttpServerErrorException e) {
            throw new BaseException(
                    "Keycloak server xətası: " + keycloakErrorDetail(e),
                    HttpStatus.BAD_GATEWAY,
                    ErrorCode.KEYCLOAK_PROVISIONING_FAILED);
        } catch (ResourceAccessException e) {
            throw new BaseException(
                    "Keycloak əlçatan deyil (şəbəkə / port): " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY,
                    ErrorCode.KEYCLOAK_PROVISIONING_FAILED);
        } catch (RestClientException e) {
            throw new BaseException(
                    "Keycloak sorğusu uğursuz: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY,
                    ErrorCode.KEYCLOAK_PROVISIONING_FAILED);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new BaseException(
                    "Keycloak üçün JSON hazırlana bilmədi",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.KEYCLOAK_PROVISIONING_FAILED);
        }
    }

    /**
     * Keycloak Admin API: əvvəl email, boşdursa username ilə axtarır.
     * Service account yoxdursa {@link Optional#empty()}.
     */
    public Optional<String> findKeycloakUserIdByEmailOrUsername(String email, String username) {
        if (provisionSkipReason().isPresent()) {
            return Optional.empty();
        }
        String adminToken = obtainServiceAccountToken();
        String base = keycloakProperties.getServerUrl().replaceAll("/$", "");
        String realm = keycloakProperties.getRealm();

        Optional<String> byEmail = findUserIdByQueryParam(base, realm, adminToken, "email", email);
        if (byEmail.isPresent()) {
            return byEmail;
        }
        if (username != null && !username.isBlank()) {
            return findUserIdByQueryParam(base, realm, adminToken, "username", username);
        }
        return Optional.empty();
    }

    /**
     * Keycloak-da istifadəçi parolunu yeniləyir. Service account yoxdursa heç nə etmir.
     */
    public void setUserPasswordInKeycloak(String keycloakUserId, String newPassword) {
        if (provisionSkipReason().isPresent()) {
            log.warn("Keycloak service account yoxdur — Keycloak parolu yenilənmədi");
            return;
        }
        if (keycloakUserId == null || keycloakUserId.isBlank() || newPassword == null) {
            return;
        }
        String adminToken = obtainServiceAccountToken();
        String base = keycloakProperties.getServerUrl().replaceAll("/$", "");
        String realm = keycloakProperties.getRealm();
        String url = base + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/reset-password";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "password");
        body.put("value", newPassword);
        body.put("temporary", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            String json = JSON.writeValueAsString(body);
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    new HttpEntity<>(json, headers),
                    Void.class);
        } catch (HttpClientErrorException e) {
            throw new BaseException(
                    "Keycloak parol yenilənmədi: " + keycloakErrorDetail(e),
                    HttpStatus.BAD_GATEWAY,
                    ErrorCode.KEYCLOAK_PROVISIONING_FAILED);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new BaseException(
                    "Keycloak üçün JSON hazırlana bilmədi",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.KEYCLOAK_PROVISIONING_FAILED);
        } catch (RestClientException e) {
            throw new BaseException(
                    "Keycloak reset-password sorğusu uğursuz: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY,
                    ErrorCode.KEYCLOAK_PROVISIONING_FAILED);
        }
    }

    private Optional<String> findUserIdByQueryParam(
            String base,
            String realm,
            String adminToken,
            String paramName,
            String paramValue) {
        if (paramValue == null || paramValue.isBlank()) {
            return Optional.empty();
        }
        String url = base
                + "/admin/realms/"
                + realm
                + "/users?"
                + paramName
                + "="
                + URLEncoder.encode(paramValue, StandardCharsets.UTF_8)
                + "&exact=true";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);
            if (response.getBody() == null) {
                return Optional.empty();
            }
            JsonNode array = JSON.readTree(response.getBody());
            if (!array.isArray() || array.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(array.get(0).path("id").asText());
        } catch (HttpClientErrorException e) {
            log.debug("Keycloak user search failed: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.debug("Keycloak user search parse error", e);
            return Optional.empty();
        }
    }

    private String resolveServiceAccountClientId() {
        String clientId = keycloakProperties.getAdminClientId();
        if (clientId == null || clientId.isBlank()) {
            clientId = keycloakProperties.getClientId();
        }
        return clientId != null ? clientId : "";
    }

    /** Admin client-id verilibsə, əvvəl admin secret; boşdursa ümumi client-secret */
    private String resolveServiceAccountSecret() {
        String adminId = keycloakProperties.getAdminClientId();
        String adminSecret = keycloakProperties.getAdminClientSecret();
        if (adminId != null && !adminId.isBlank()) {
            if (adminSecret != null && !adminSecret.isBlank()) {
                return adminSecret;
            }
            return keycloakProperties.getClientSecret() != null
                    ? keycloakProperties.getClientSecret()
                    : "";
        }
        return keycloakProperties.getClientSecret() != null
                ? keycloakProperties.getClientSecret()
                : "";
    }

    private String obtainServiceAccountToken() {
        String clientId = resolveServiceAccountClientId();
        String secret = resolveServiceAccountSecret();
        if (clientId.isBlank()) {
            throw new BaseException(
                    "Keycloak admin client-id təyin olunmayıb (keycloak.admin-client-id və ya keycloak.client-id)",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.KEYCLOAK_PROVISIONING_FAILED);
        }
        if (secret.isBlank()) {
            throw new BaseException(
                    "Keycloak admin üçün client-secret lazımdır (service account)",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.KEYCLOAK_PROVISIONING_FAILED);
        }

        String base = keycloakProperties.getServerUrl().replaceAll("/$", "");
        String tokenUrl = base + "/realms/" + keycloakProperties.getRealm()
                + "/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", secret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    tokenUrl,
                    new HttpEntity<>(form, headers),
                    Map.class);
            Map<?, ?> respBody = response.getBody();
            if (respBody == null || respBody.get("access_token") == null) {
                throw new BaseException(
                        "Keycloak service account token alına bilmədi",
                        HttpStatus.BAD_GATEWAY,
                        ErrorCode.KEYCLOAK_PROVISIONING_FAILED);
            }
            return (String) respBody.get("access_token");
        } catch (HttpClientErrorException e) {
            throw new BaseException(
                    "Keycloak service account: " + keycloakErrorDetail(e)
                            + " (Client-də Service accounts aktiv və realm-management → manage-users təyin edin)",
                    HttpStatus.BAD_GATEWAY,
                    ErrorCode.KEYCLOAK_PROVISIONING_FAILED);
        } catch (ResourceAccessException e) {
            throw new BaseException(
                    "Keycloak əlçatan deyil (token): " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY,
                    ErrorCode.KEYCLOAK_PROVISIONING_FAILED);
        } catch (RestClientException e) {
            throw new BaseException(
                    "Keycloak token sorğusu uğursuz: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY,
                    ErrorCode.KEYCLOAK_PROVISIONING_FAILED);
        }
    }

    private static String keycloakErrorDetail(HttpStatusCodeException e) {
        String raw = e.getResponseBodyAsString();
        if (raw == null || raw.isBlank()) {
            return e.getStatusCode().toString();
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
            // ignore
        }
        return raw.length() > 200 ? raw.substring(0, 200) + "…" : raw;
    }
}
