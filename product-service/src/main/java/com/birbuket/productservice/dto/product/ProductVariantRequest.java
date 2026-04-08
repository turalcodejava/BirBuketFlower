package com.birbuket.productservice.dto.product;

import com.birbuket.productservice.enums.ProductColor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantRequest {

    @NotNull(message = "Price boş ola bilməz")
    @Positive(message = "Price 0-dan böyük olmalıdır")
    private BigDecimal price;  // Məhsulun qiyməti

    /** Create zamanı boş ola bilər; saxlanandan sonra set olunur */
    private Long productId;

    @NotNull(message = "Color boş ola bilməz")
    private ProductColor color; // Rəng enum ilə
}