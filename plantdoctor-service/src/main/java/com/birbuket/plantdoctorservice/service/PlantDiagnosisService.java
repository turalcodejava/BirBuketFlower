package com.birbuket.plantdoctorservice.service;

import com.birbuket.plantdoctorservice.dto.AgronomistReplyRequest;
import com.birbuket.plantdoctorservice.dto.AddressResponse;
import com.birbuket.plantdoctorservice.dto.CreateAddressRequest;
import com.birbuket.plantdoctorservice.dto.CreateConsultationRequest;
import com.birbuket.plantdoctorservice.dto.CreateDiagnosisRequest;
import com.birbuket.plantdoctorservice.dto.CreateDiagnosisResponse;
import com.birbuket.plantdoctorservice.dto.DiagnosisDetailResponse;
import com.birbuket.plantdoctorservice.dto.ReserveDiagnosisRequest;
import com.birbuket.plantdoctorservice.dto.StoreLocationResponse;
import com.birbuket.plantdoctorservice.enums.DiagnosisKind;

import java.util.List;

public interface PlantDiagnosisService {

    CreateDiagnosisResponse createDiagnosis(CreateDiagnosisRequest request);

    CreateDiagnosisResponse createConsultation(CreateConsultationRequest request);

    DiagnosisDetailResponse getHomeVisit(Long id);

    DiagnosisDetailResponse getConsultation(Long id);

    DiagnosisDetailResponse getDiagnosis(Long id, DiagnosisKind kind);

    List<DiagnosisDetailResponse> getAllDiagnoses();

    List<DiagnosisDetailResponse> getPendingDiagnoses();

    List<DiagnosisDetailResponse> getPendingHomeVisits();

    List<DiagnosisDetailResponse> getPendingConsultations();

    List<DiagnosisDetailResponse> getReservedDiagnoses(Long agronomistId);

    StoreLocationResponse getStoreLocation();

    List<AddressResponse> getAddresses(Long userId);

    AddressResponse createAddress(CreateAddressRequest request);

    DiagnosisDetailResponse reserveByAgronomist(Long id, ReserveDiagnosisRequest request);

    DiagnosisDetailResponse reserveConsultationByAgronomist(Long id, ReserveDiagnosisRequest request);

    DiagnosisDetailResponse replyHomeVisit(Long id, AgronomistReplyRequest request);

    DiagnosisDetailResponse replyConsultation(Long id, AgronomistReplyRequest request);
}
