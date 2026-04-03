package com.birbuket.productservice.models;

import com.birbuket.productservice.enums.ProductType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "product_items")
public class ProductItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank(message = "Product name boş ola bilməz")
    @Size(min = 2, max = 100, message = "Product name 2-100 simvol arası olmalıdır")
    @Column(nullable = false, unique = true, length = 100)
    String name;

    @NotNull(message = "Price boş ola bilməz")
    @Positive(message = "Price 0-dan böyük olmalıdır")
    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal price;

    @NotBlank(message = "Image URL boş ola bilməz")
    @Size(max = 500)
    @Pattern(
            regexp = "^(http|https)://.*$",
            message = "Image URL düzgün formatda olmalıdır"
    )
    @Column(nullable = false, unique = true, length = 500)
    String imageUrl;

    @NotNull(message = "Product type boş ola bilməz")
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    ProductType productType;
}