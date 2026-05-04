package com.birbuket.orderservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "payment.epoint")
public class EpointProperties {
    private boolean enabled = false;
    private String requestUrl = "https://epoint.az/api/1/request";
    private String publicKey;
    private String privateKey;
    private String successRedirectUrl = "http://localhost:3000/payment/success";
    private String errorRedirectUrl = "http://localhost:3000/payment/error";
}
