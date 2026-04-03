package com.birbuket.productservice.dto.product;

import com.birbuket.productservice.enums.ProductColor;

import java.math.BigDecimal;

public class ProductVariantResponse {
    private Long id;
    private BigDecimal price;
    private ProductColor color;
}
