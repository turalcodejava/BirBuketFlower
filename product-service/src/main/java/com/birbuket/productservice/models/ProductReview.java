package com.birbuket.productservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "product_review")
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Size(max = 500, message = "Review maksimum 500 simvol ola bilər")
    @Column(length = 500)
    String review;

    @NotNull(message = "Rating boş ola bilməz")
    @Min(value = 1, message = "Rating ən azı 1 olmalıdır")
    @Max(value = 5, message = "Rating ən çox 5 ola bilər")
    @Column(nullable = false)
    Integer rate;

    @NotNull(message = "Product boş ola bilməz")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}