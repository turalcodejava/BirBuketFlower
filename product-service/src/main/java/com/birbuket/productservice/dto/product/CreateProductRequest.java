package com.birbuket.productservice.dto.product;

import com.birbuket.productservice.enums.ProductSize;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Product name boş ola bilməz")
    @Size(min = 2, max = 150)
    private String productName;  // Məhsulun adı

    @Size(max = 1000)
    private String description;  // Ümumi məlumat

    @Size(max = 500)
    private String composition;  // Məhsulun tərkibi

    @PositiveOrZero(message = "Discount percentage mənfi ola bilməz")
    @Max(value = 100, message = "Discount 100%-dən böyük ola bilməz")
    private BigDecimal discountPercentage;  // Endirim faizi

    private boolean active = true;  // Məhsul aktivliyi

    private boolean isSingle = false; // Tək məhsul mu, yoxsa set?

    @DecimalMin("0.0")
    @DecimalMax("5.0")
    private BigDecimal rating = BigDecimal.ZERO; // Reytinq (opsional, default 0.0)

    @PositiveOrZero
    private Integer reviewCount = 0; // Review sayı (opsional, default 0)

    @NotBlank(message = "Slug boş ola bilməz")
    @Size(max = 200)
    private String slug; // SEO / URL üçün ad

    @Size(max = 50)
    private List<ProductSize> size;  // Məhsulun ölçüsü

    @Column(unique = true, length = 50, nullable = false)
    private String sku; // Məhsulun kodlaşdırılması üçün

    @ArraySchema(
            arraySchema = @Schema(description = "Məhsul şəkilləri"),
            schema = @Schema(type = "string", format = "binary")
    )
    private MultipartFile[] images;

    private List<ProductVariantRequest> productVariants;

    @NotNull(message = "Product category boş ola bilməz")
    private Long productCategoryId; // Hangi kateqoriyaya aid olduğu
}