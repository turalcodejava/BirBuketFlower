package com.birbuket.plantdoctorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private Long userId;
    private String phoneNumber;
    private String fullAddressLine;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
}
