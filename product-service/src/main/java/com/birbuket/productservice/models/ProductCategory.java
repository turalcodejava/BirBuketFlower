package com.birbuket.productservice.models;

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
@Table(name = "product_categories")

// Homepage Category
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank(message = "Title boş ola bilməz")
    @Size(min = 2, max = 50, message = "Title 2-50 simvol arası olmalıdır")
    @Column(nullable = false, unique = true, length = 50)
    String title;

    @NotBlank(message = "Subtitle boş ola bilməz")
    @Size(max = 200, message = "Subtitle maksimum 200 simvol ola bilər")
    @Column(nullable = false,length = 200)
    String subtitle;

    String imageUrl;
}