package com.birbuket.plantdoctorservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAddressRequest {
    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "phoneNumber is required")
    private String phoneNumber;

    @NotBlank(message = "fullAddressLine is required")
    private String fullAddressLine;

    private Double latitude;
    private Double longitude;
}
