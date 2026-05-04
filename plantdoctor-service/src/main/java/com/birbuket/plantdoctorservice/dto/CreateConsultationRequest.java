package com.birbuket.plantdoctorservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConsultationRequest {
    @NotNull(message = "userId is required")
    private Long userId;

    @Size(max = 160)
    private String contactEmail;

    @NotBlank(message = "plantType is required")
    private String plantType;

    @NotBlank(message = "symptoms is required")
    private String symptoms;

    @NotNull(message = "image is required")
    private MultipartFile image;

    private String specialNote;
}
