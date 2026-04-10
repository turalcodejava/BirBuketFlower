package com.birbuket.productservice.dto.product.variants;

import com.birbuket.productservice.enums.ProductColor;
import com.birbuket.productservice.enums.ProductSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateVariantsRequest {

    @NotNull(message = "Price boş ola bilməz")
    @Positive(message = "Price 0-dan böyük olmalıdır")
    private BigDecimal price;  // Məhsulun qiyməti

    @NotNull(message = "Size boş ola bilməz")
    private ProductSize size;  // Məhsulun ölçüsü


    @NotNull(message = "Color boş ola bilməz")
    private ProductColor color; // Rəng enum ilə
}
