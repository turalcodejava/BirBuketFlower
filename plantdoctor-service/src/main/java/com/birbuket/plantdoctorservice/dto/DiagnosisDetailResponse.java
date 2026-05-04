package com.birbuket.plantdoctorservice.dto;

import com.birbuket.plantdoctorservice.enums.DiagnosisKind;
import com.birbuket.plantdoctorservice.enums.DiagnosisStatus;
import com.birbuket.plantdoctorservice.enums.PlantCountRange;
import com.birbuket.plantdoctorservice.enums.VisitTimeSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisDetailResponse {
    private Long id;
    private DiagnosisKind kind;
    private String plantType;
    private String email;
    private String symptoms;
    private String imageUrl;
    private String phoneNumber;
    private String fullAddressLine;
    private String specialNote;
    private Long agronomistId;
    private PlantCountRange plantCountRange;
    private LocalDate visitDate;
    private VisitTimeSlot visitTimeSlot;
    private BigDecimal distanceKm;
    private BigDecimal baseVisitFee;
    private BigDecimal plantCountFee;
    private BigDecimal transportFee;
    private BigDecimal totalFee;
    private DiagnosisStatus status;
    private String agronomistResponse;
    private LocalDateTime reservedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
