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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDiagnosisResponse {
    private Long id;
    private DiagnosisKind kind;
    private DiagnosisStatus status;
    private String imageUrl;
    private String diagnosisLink;
    private boolean emailSent;
    private PlantCountRange plantCountRange;
    private LocalDate visitDate;
    private VisitTimeSlot visitTimeSlot;
    private BigDecimal baseVisitFee;
    private BigDecimal plantCountFee;
    private BigDecimal transportFee;
    private BigDecimal totalFee;
}
