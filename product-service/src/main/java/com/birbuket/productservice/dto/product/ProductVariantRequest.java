package com.birbuket.productservice.dto.product;

import com.birbuket.productservice.enums.ProductColor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductVariantRequest {

    @NotNull(message = "Price boş ola bilməz")
    @Positive(message = "Price 0-dan böyük olmalıdır")
    private BigDecimal price;  // Məhsulun qiyməti

    /** Create zamanı boş ola bilər; saxlanandan sonra set olunur */
    private Long productId;

    @NotNull(message = "Color boş ola bilməz")
    private ProductColor color; // Rəng enum ilə
}