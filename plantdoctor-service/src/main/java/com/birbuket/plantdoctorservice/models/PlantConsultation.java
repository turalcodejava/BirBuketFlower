package com.birbuket.plantdoctorservice.models;

import com.birbuket.plantdoctorservice.enums.DiagnosisStatus;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "plant_consultation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantConsultation {

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

    @Column(name = "special_note", length = 1000)
    private String specialNote;

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
