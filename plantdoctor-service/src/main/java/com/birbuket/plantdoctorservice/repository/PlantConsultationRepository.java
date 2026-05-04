package com.birbuket.plantdoctorservice.repository;

import com.birbuket.plantdoctorservice.enums.DiagnosisStatus;
import com.birbuket.plantdoctorservice.models.PlantConsultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantConsultationRepository extends JpaRepository<PlantConsultation, Long> {

    List<PlantConsultation> findAllByOrderByCreatedAtDesc();

    List<PlantConsultation> findAllByStatusOrderByCreatedAtDesc(DiagnosisStatus status);
}
