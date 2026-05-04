package com.birbuket.orderservice.payment;

import com.birbuket.orderservice.config.EpointProperties;
import com.birbuket.orderservice.models.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EpointPaymentService {

    private final EpointProperties epointProperties;
    private final RestTemplateBuilder restTemplateBuilder;

    public PaymentInitResult createPaymentRequest(Order order) {
        if (!epointProperties.isEnabled()) {
            throw new IllegalStateException("Epoint payment is disabled");
        }
        if (isBlank(epointProperties.getPublicKey()) || isBlank(epointProperties.getPrivateKey())) {
            throw new IllegalStateException("Epoint keys are not configured");
        }

        RestTemplate restTemplate = restTemplateBuilder.build();
        String paymentRef = "ORD-" + order.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("public_key", epointProperties.getPublicKey());
        payload.put("private_key", epointProperties.getPrivateKey());
        payload.put("amount", formatAmount(order.getTotalPrice()));
        payload.put("currency", "AZN");
        payload.put("language", "az");
        payload.put("order_id", String.valueOf(order.getId()));
        payload.put("description", "BirBuket sifaris odenisi");
        payload.put("success_redirect_url", epointProperties.getSuccessRedirectUrl());
        payload.put("error_redirect_url", epointProperties.getErrorRedirectUrl());
        payload.put("reference", paymentRef);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                epointProperties.getRequestUrl(),
                new HttpEntity<>(payload, headers),
                Map.class);

        Map<?, ?> body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("Epoint response is empty");
        }
        String paymentUrl = extractPaymentUrl(body);
        if (isBlank(paymentUrl)) {
            log.error("Epoint response does not contain payment URL: {}", body);
            throw new IllegalStateException("Epoint payment URL not found");
        }

        return new PaymentInitResult(paymentUrl, paymentRef);
    }

    private String formatAmount(BigDecimal amount) {
        return amount == null ? "0.00" : amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String extractPaymentUrl(Map<?, ?> body) {
        Object direct = body.get("redirect_url");
        if (direct instanceof String s && !s.isBlank()) return s;
        Object paymentUrl = body.get("payment_url");
        if (paymentUrl instanceof String s && !s.isBlank()) return s;
        Object url = body.get("url");
        if (url instanceof String s && !s.isBlank()) return s;
        Object data = body.get("data");
        if (data instanceof Map<?, ?> map) {
            Object nested = map.get("redirect_url");
            if (nested instanceof String s && !s.isBlank()) return s;
            nested = map.get("payment_url");
            if (nested instanceof String s && !s.isBlank()) return s;
            nested = map.get("url");
            if (nested instanceof String s && !s.isBlank()) return s;
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record PaymentInitResult(String paymentUrl, String paymentReference) {}
}
