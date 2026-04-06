package com.birbuket.productservice.dto.product;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductImageRequest {

    @NotNull(message = "Şəkil faylı boş ola bilməz")
    private MultipartFile image;
}
