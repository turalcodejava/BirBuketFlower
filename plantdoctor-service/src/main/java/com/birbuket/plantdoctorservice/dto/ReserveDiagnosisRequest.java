package com.birbuket.plantdoctorservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveDiagnosisRequest {
    @NotNull(message = "agronomistId is required")
    private Long agronomistId;
}
