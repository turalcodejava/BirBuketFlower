package com.birbuket.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayOrderRequest {
    @NotNull(message = "orderId is required")
    private Long orderId;
}
