package com.birbuket.productservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "colors")
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Color name boş ola bilməz")
    @Column(nullable = false, length = 50)
    private String colorName;


    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Hex code düzgün formatda olmalıdır")
    @Column(nullable = false, length = 7)
    private String hexCode;
}
