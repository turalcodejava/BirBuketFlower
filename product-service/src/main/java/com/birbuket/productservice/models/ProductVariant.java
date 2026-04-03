package com.birbuket.productservice.models;

import com.birbuket.productservice.enums.ProductColor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Price boş ola bilməz")
    @Positive(message = "Price 0-dan böyük olmalıdır")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Color boş ola bilməz")
    @Enumerated(EnumType.STRING) // DB-də enum adı saxlanacaq
    @Column(length = 20, nullable = false)
    private ProductColor color;
}