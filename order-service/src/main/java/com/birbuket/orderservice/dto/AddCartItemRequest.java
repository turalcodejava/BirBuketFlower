package com.birbuket.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCartItemRequest {

    @NotNull
    private Long productId;

    @NotBlank
    private String productName;

    @NotNull
    private BigDecimal unitPrice;

    @NotNull
    @Min(1)
    private Integer quantity;
}
