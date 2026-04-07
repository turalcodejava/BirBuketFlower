package com.birbuket.productservice.dto.product;

import com.birbuket.productservice.enums.ProductColor;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantResponse {
    private Long id;
    private BigDecimal price;
    private ProductColor color;
}
