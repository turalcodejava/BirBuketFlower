package com.birbuket.plantdoctorservice.repository;

import com.birbuket.plantdoctorservice.enums.DiagnosisStatus;
import com.birbuket.plantdoctorservice.models.HomeVisitBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeVisitRepository extends JpaRepository<HomeVisitBooking, Long> {

    List<HomeVisitBooking> findAllByOrderByCreatedAtDesc();

    List<HomeVisitBooking> findAllByStatusOrderByCreatedAtDesc(DiagnosisStatus status);

    List<HomeVisitBooking> findAllByStatusAndAgronomistIdOrderByCreatedAtDesc(
            DiagnosisStatus status,
            Long agronomistId
    );
}
