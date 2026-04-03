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

    @NotNull(message = "Product boş ola bilməz")
    private Long productId;     // Hansı məhsula aid olduğu

    @NotNull(message = "Color boş ola bilməz")
    private ProductColor color; // Rəng enum ilə
}