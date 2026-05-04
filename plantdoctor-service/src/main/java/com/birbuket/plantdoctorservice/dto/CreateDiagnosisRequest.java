package com.birbuket.plantdoctorservice.dto;

import com.birbuket.plantdoctorservice.enums.PlantCountRange;
import com.birbuket.plantdoctorservice.enums.VisitTimeSlot;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDiagnosisRequest {
    @NotNull(message = "userId is required")
    private Long userId;

    /** Dolu olarsa məktublar və DB-də bu ünvandan istifadə olunur (auth əlçatan deyilsə də). Servis tərəfindən format yoxlanır. */
    @Size(max = 160)
    private String contactEmail;

    @NotBlank(message = "plantType is required")
    private String plantType;

    @NotBlank(message = "symptoms is required")
    private String symptoms;

    private Long addressId;
    private String phoneNumber;
    private String fullAddressLine;
    private Double latitude;
    private Double longitude;
    private boolean saveAddress;

    @NotNull(message = "plantCountRange is required")
    private PlantCountRange plantCountRange;

    @NotNull(message = "visitDate is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate visitDate;

    @NotNull(message = "visitTimeSlot is required")
    private VisitTimeSlot visitTimeSlot;

    private String specialNote;
    private Double distanceKm;
}
