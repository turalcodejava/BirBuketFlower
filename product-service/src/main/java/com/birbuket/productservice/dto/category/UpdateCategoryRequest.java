package com.birbuket.productservice.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@Data
public class UpdateCategoryRequest {

    @NotBlank(message = "Title boş ola bilməz")
    @Size(min = 2, max = 50, message = "Title 2-50 simvol arası olmalıdır")
    String title;

    @NotBlank(message = "Subtitle boş ola bilməz")
    @Size(max = 200, message = "Subtitle maksimum 200 simvol ola bilər")
    String subtitle;

    MultipartFile image;
}
