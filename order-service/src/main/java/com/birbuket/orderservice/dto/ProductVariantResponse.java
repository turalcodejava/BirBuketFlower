package com.birbuket.orderservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantResponse {
    private Long id;
    private BigDecimal price;
    private String size;
    private String color;
}
