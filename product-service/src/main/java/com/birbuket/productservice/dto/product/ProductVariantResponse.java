package com.birbuket.productservice.dto.product;

import com.birbuket.productservice.enums.ProductColor;
import com.birbuket.productservice.enums.ProductSize;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductVariantResponse {
    private Long id;
    private BigDecimal price;
    private ProductSize size;  // Məhsulun ölçüsü
    private ProductColor color;
}
