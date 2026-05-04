package com.birbuket.plantdoctorservice.repository;

import com.birbuket.plantdoctorservice.models.PlantVisitAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantVisitAddressRepository extends JpaRepository<PlantVisitAddress, Long> {
    List<PlantVisitAddress> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
