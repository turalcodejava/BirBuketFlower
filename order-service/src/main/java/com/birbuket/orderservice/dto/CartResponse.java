package com.birbuket.orderservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private Long userId;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
}
