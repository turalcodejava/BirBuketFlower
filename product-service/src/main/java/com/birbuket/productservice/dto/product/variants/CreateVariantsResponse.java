package com.birbuket.productservice.dto.product.variants;

import com.birbuket.productservice.enums.ProductColor;
import com.birbuket.productservice.enums.ProductSize;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class CreateVariantsResponse {

    private Long id;
    private Long productId;
    private BigDecimal price;  // Məhsulun qiyməti
    private ProductSize size;  // Məhsulun ölçüsü
    private ProductColor color; // Rəng enum ilə
}
