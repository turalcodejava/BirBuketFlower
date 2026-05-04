package com.birbuket.plantdoctorservice.models;

import com.birbuket.plantdoctorservice.enums.DiagnosisStatus;
import com.birbuket.plantdoctorservice.enums.PlantCountRange;
import com.birbuket.plantdoctorservice.enums.VisitTimeSlot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "plant_home_visit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeVisitBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plant_type", nullable = false, length = 120)
    private String plantType;

    @Column(name = "email", nullable = false, length = 160)
    private String email;

    @Column(name = "symptoms", nullable = false, length = 3000)
    private String symptoms;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "image_path", nullable = false, length = 500)
    private String imagePath;

    @Column(name = "agronomist_response", length = 4000)
    private String agronomistResponse;

    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "full_address_line", length = 500)
    private String fullAddressLine;

    @Column(name = "special_note", length = 1000)
    private String specialNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "plant_count_range", length = 20)
    private PlantCountRange plantCountRange;

    @Column(name = "visit_date")
    private LocalDate visitDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_time_slot", length = 20)
    private VisitTimeSlot visitTimeSlot;

    @Column(name = "distance_km", precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "base_visit_fee", precision = 10, scale = 2)
    private BigDecimal baseVisitFee;

    @Column(name = "plant_count_fee", precision = 10, scale = 2)
    private BigDecimal plantCountFee;

    @Column(name = "transport_fee", precision = 10, scale = 2)
    private BigDecimal transportFee;

    @Column(name = "total_fee", precision = 10, scale = 2)
    private BigDecimal totalFee;

    @Column(name = "customer_latitude")
    private Double customerLatitude;

    @Column(name = "customer_longitude")
    private Double customerLongitude;

    @Column(name = "agronomist_id")
    private Long agronomistId;

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DiagnosisStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = DiagnosisStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
