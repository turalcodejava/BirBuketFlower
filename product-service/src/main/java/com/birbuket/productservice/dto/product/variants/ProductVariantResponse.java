package com.birbuket.productservice.dto.product.variants;

import com.birbuket.productservice.enums.ProductColor;
import com.birbuket.productservice.enums.ProductSize;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantResponse {
    private Long id;
    private BigDecimal price;
    private ProductSize size;  // Məhsulun ölçüsü
    private ProductColor color;
}
