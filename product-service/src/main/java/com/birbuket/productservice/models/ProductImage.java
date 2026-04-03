package com.birbuket.productservice.models;

import com.birbuket.productservice.models.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank(message = "Image URL boş ola bilməz")
    @Size(max = 500)
    @Pattern(
            regexp = "^(http|https)://.*$",
            message = "Image URL düzgün formatda olmalıdır"
    )
    @Column(name = "image_url", nullable = false, length = 500)
    String imageUrl;

    @NotNull(message = "Product boş ola bilməz")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    Product product;
}