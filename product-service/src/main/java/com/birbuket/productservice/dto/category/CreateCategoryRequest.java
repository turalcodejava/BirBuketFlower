package com.birbuket.productservice.dto.category;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCategoryRequest {

    @NotBlank(message = "Title boş ola bilməz")
    @Size(min = 2, max = 50, message = "Title 2-50 simvol arası olmalıdır")
    String title;

    @NotBlank(message = "Subtitle boş ola bilməz")
    @Size(max = 200, message = "Subtitle maksimum 200 simvol ola bilər")
    String subtitle;

    MultipartFile image;
}