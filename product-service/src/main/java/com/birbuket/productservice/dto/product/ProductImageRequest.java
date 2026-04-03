package com.birbuket.productservice.dto.product;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * Yeni məhsul yaradarkən productId hələ yoxdur; yalnız fayl lazımdır.
 */
@Data
public class ProductImageRequest {

    @NotNull(message = "Şəkil faylı boş ola bilməz")
    private MultipartFile image;
}
