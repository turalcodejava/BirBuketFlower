package com.birbuket.productservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "products")

// Ümumi məhsulların siyahısı
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank(message = "Product name boş ola bilməz")
    @Size(min = 2, max = 150)
    @Column(name = "product_name", nullable = false, unique = true, length = 150)
    String productName; // Mehsulun adi

    @Size(max = 1000)
    @Column(length = 1000)
    String description;  // Umumi melumat

    @Size(max = 500)
    @Column(length = 500)
    String composition; // Mehsulun terkibi

    @PositiveOrZero(message = "Discount percentage mənfi ola bilməz")
    @Max(value = 100, message = "Discount 100%-dən böyük ola bilməz")
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    BigDecimal discountPercentage;  // Endirim faizi

    @Column(nullable = false)
    boolean active = true;  // Mehsulun aktivliyi

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime createdAt;  // Yaranma tarixi

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @Column(name = "is_single")
    boolean isSingle = false;

    @DecimalMin("0.0")
    @DecimalMax("5.0")
    @Column(precision = 2, scale = 1)
    BigDecimal rating = BigDecimal.ZERO;

    @PositiveOrZero
    Integer reviewCount = 0;

    @Column(unique = true, length = 200, nullable = false)
    String slug; // SEO ve URL ucun ad

    @Size(max = 50)
    @Column(length = 50)
    String size;  // Mehsulun olcusu

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "product", orphanRemoval = true)
    @Builder.Default
    List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<ProductSize> productSizes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_category_id")
    ProductCategory productCategory;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<ProductReview> productReviews = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<ProductVariant> productVariants = new ArrayList<>();

}