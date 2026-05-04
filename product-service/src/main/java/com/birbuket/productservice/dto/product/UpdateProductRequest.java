package com.birbuket.productservice.dto.product;

import com.birbuket.productservice.dto.product.variants.ProductVariantRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;


@Data
public class UpdateProductRequest {

    @Size(min = 2, max = 150)
    private String productName;  // Məhsulun adı

    @Size(max = 1000)
    private String description;  // Ümumi məlumat

    @Size(max = 500)
    private String composition;  // Məhsulun tərkibi

    @PositiveOrZero(message = "Discount percentage mənfi ola bilməz")
    @Max(value = 100, message = "Discount 100%-dən böyük ola bilməz")
    private BigDecimal discountPercentage;  // Endirim faizi

    private Boolean active;  // Məhsul aktivliyi

    private Boolean isSingle; // Tək məhsul mu, yoxsa set?

    @DecimalMin("0.0")
    @DecimalMax("5.0")
    private BigDecimal rating = BigDecimal.ZERO; // Reytinq (opsional, default 0.0)

    @PositiveOrZero
    private Integer reviewCount = 0; // Review sayı (opsional, default 0)

    @Size(max = 200)
    private String slug; // SEO / URL üçün ad

    private String sku; // Məhsulun kodlaşdırılması üçün

    @Valid
    private List<ProductVariantRequest> productVariants;

    private Long productCategoryId;
}
