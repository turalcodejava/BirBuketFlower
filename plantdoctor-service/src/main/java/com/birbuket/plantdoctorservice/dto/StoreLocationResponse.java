package com.birbuket.plantdoctorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreLocationResponse {
    private String name;
    private String addressLine;
    private Double latitude;
    private Double longitude;
}
