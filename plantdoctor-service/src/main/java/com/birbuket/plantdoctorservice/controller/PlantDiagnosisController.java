package com.birbuket.plantdoctorservice.controller;

import com.birbuket.common.dto.ApiResponse;
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
import com.birbuket.plantdoctorservice.service.PlantDiagnosisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/plantdoctor")
@RequiredArgsConstructor
@Tag(name = "Plant Doctor", description = "Plant diagnosis APIs")
public class PlantDiagnosisController {

    private final PlantDiagnosisService diagnosisService;

    @PostMapping(value = "/diagnosis", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create home visit reservation (JSON) — plant_home_visit cədvəli")
    public ResponseEntity<ApiResponse<CreateDiagnosisResponse>> createDiagnosis(
            @Valid @RequestBody CreateDiagnosisRequest request
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(
                diagnosisService.createDiagnosis(request),
                "Ev ziyarəti rezervasiyanız qeydə alındı."
        ));
    }

    @PostMapping(value = "/consultation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create online plant consultation (photo + text) — plant_consultation cədvəli")
    public ResponseEntity<ApiResponse<CreateDiagnosisResponse>> createConsultation(
            @Valid @ModelAttribute CreateConsultationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(
                diagnosisService.createConsultation(request),
                "Bitki həkimi müraciətiniz qəbul edildi. Tezliklə emailinizə cavab göndəriləcək."
        ));
    }

    @GetMapping("/addresses")
    @Operation(summary = "Get saved addresses by user")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(@RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success(diagnosisService.getAddresses(userId)));
    }

    @GetMapping("/store-location")
    @Operation(summary = "Get static store location")
    public ResponseEntity<ApiResponse<StoreLocationResponse>> getStoreLocation() {
        return ResponseEntity.ok(ApiResponse.success(diagnosisService.getStoreLocation()));
    }

    @PostMapping("/addresses")
    @Operation(summary = "Save new address for user")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(@Valid @RequestBody CreateAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(diagnosisService.createAddress(request)));
    }

    @GetMapping("/home-visit/{id:\\d+}")
    @Operation(summary = "Ev ziyarəti rekordu — plant_home_visit.id")
    public ResponseEntity<ApiResponse<DiagnosisDetailResponse>> getHomeVisitById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(diagnosisService.getHomeVisit(id)));
    }

    @GetMapping("/consultation/{id:\\d+}")
    @Operation(summary = "Onlayn müraciət — plant_consultation.id")
    public ResponseEntity<ApiResponse<DiagnosisDetailResponse>> getConsultationById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(diagnosisService.getConsultation(id)));
    }

    /**
     * @param kind HOME_VISIT / CONSULTATION — hansi cədvəldən oxunacağını seçir (eyni rəqəm id-lər təsadüfü toqquşmur).
     */
    @GetMapping("/diagnosis/{id:\\d+}")
    @Operation(summary = "Ümumi sorğu: kind ilə hansı cədvəl (köhnə inteqrasiya)")
    public ResponseEntity<ApiResponse<DiagnosisDetailResponse>> getDiagnosis(
            @PathVariable Long id,
            @RequestParam DiagnosisKind kind
    ) {
        return ResponseEntity.ok(ApiResponse.success(diagnosisService.getDiagnosis(id, kind)));
    }

    @GetMapping("/diagnosis/pending")
    @Operation(summary = "List all pending requests (home visit + consultation)")
    public ResponseEntity<ApiResponse<List<DiagnosisDetailResponse>>> getPendingDiagnoses() {
        return ResponseEntity.ok(ApiResponse.success(diagnosisService.getPendingDiagnoses()));
    }

    @GetMapping("/home-visit/pending")
    @Operation(summary = "List pending home visit reservations only")
    public ResponseEntity<ApiResponse<List<DiagnosisDetailResponse>>> getPendingHomeVisits() {
        return ResponseEntity.ok(ApiResponse.success(diagnosisService.getPendingHomeVisits()));
    }

    @GetMapping("/consultation/pending")
    @Operation(summary = "List pending online consultations only")
    public ResponseEntity<ApiResponse<List<DiagnosisDetailResponse>>> getPendingConsultations() {
        return ResponseEntity.ok(ApiResponse.success(diagnosisService.getPendingConsultations()));
    }

    @GetMapping("/diagnosis")
    @Operation(summary = "List all diagnosis requests")
    public ResponseEntity<ApiResponse<List<DiagnosisDetailResponse>>> getAllDiagnoses() {
        return ResponseEntity.ok(ApiResponse.success(diagnosisService.getAllDiagnoses()));
    }

    @GetMapping("/agronomist/diagnosis")
    @Operation(summary = "List all diagnosis requests for agronomist panel")
    public ResponseEntity<ApiResponse<List<DiagnosisDetailResponse>>> getAllDiagnosesForAgronomist() {
        return ResponseEntity.ok(ApiResponse.success(diagnosisService.getAllDiagnoses()));
    }

    @GetMapping("/agronomist/inbox/pending")
    @Operation(summary = "Agronomist: bütün gözləyənlər (ev ziyarəti + onlayn sual)")
    public ResponseEntity<ApiResponse<List<DiagnosisDetailResponse>>> agronomistInboxPending() {
        return ResponseEntity.ok(ApiResponse.success(diagnosisService.getPendingDiagnoses()));
    }

    @GetMapping("/agronomist/inbox/home-visits")
    @Operation(summary = "Agronomist: yalnız gözləyən ev ziyarəti rezervləri")
    public ResponseEntity<ApiResponse<List<DiagnosisDetailResponse>>> agronomistInboxHomeVisits() {
        return ResponseEntity.ok(ApiResponse.success(diagnosisService.getPendingHomeVisits()));
    }

    @GetMapping("/agronomist/inbox/consultations")
    @Operation(summary = "Agronomist: yalnız gözləyən onlayn müraciətlər")
    public ResponseEntity<ApiResponse<List<DiagnosisDetailResponse>>> agronomistInboxConsultations() {
        return ResponseEntity.ok(ApiResponse.success(diagnosisService.getPendingConsultations()));
    }

    @GetMapping("/agronomist/inbox/{id:\\d+}")
    @Operation(summary = "Detal üçün kind parametri məcburidir — hansi cədvəl")
    public ResponseEntity<ApiResponse<DiagnosisDetailResponse>> agronomistInboxItem(
            @PathVariable Long id,
            @RequestParam DiagnosisKind kind
    ) {
        return ResponseEntity.ok(ApiResponse.success(diagnosisService.getDiagnosis(id, kind)));
    }

    @GetMapping(value = "/agronomist/panel", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Simple agronomist panel page")
    public ResponseEntity<String> agronomistPanel() throws Exception {
        ClassPathResource resource = new ClassPathResource("static/agronomist-panel.html");
        String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return ResponseEntity.ok(html);
    }

    @PatchMapping("/agronomist/home-visit/{id}/reserve")
    @Operation(summary = "Ev ziyarətini aqronom rezerv edir (plant_home_visit)")
    public ResponseEntity<ApiResponse<DiagnosisDetailResponse>> reserveHomeVisit(
            @PathVariable Long id,
            @Valid @RequestBody ReserveDiagnosisRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                diagnosisService.reserveByAgronomist(id, request),
                "Rezervasiya ugurla edildi."
        ));
    }

    @PatchMapping("/agronomist/consultation/{id}/reserve")
    @Operation(summary = "Onlayn müraciəti aqronom qəbul edir (PENDING→RESERVED); istifadəçiyə e-poçt gedir")
    public ResponseEntity<ApiResponse<DiagnosisDetailResponse>> reserveConsultation(
            @PathVariable Long id,
            @Valid @RequestBody ReserveDiagnosisRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                diagnosisService.reserveConsultationByAgronomist(id, request),
                "Müraciət qəbul olundu."
        ));
    }

    @PatchMapping("/agronomist/home-visit/{id}/reply")
    @Operation(summary = "Ev ziyarətinə cavab + tamamla")
    public ResponseEntity<ApiResponse<DiagnosisDetailResponse>> replyHomeVisit(
            @PathVariable Long id,
            @Valid @RequestBody AgronomistReplyRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                diagnosisService.replyHomeVisit(id, request),
                "Agronom cavabi ugurla yadda saxlanildi."
        ));
    }

    @PatchMapping("/agronomist/consultation/{id}/reply")
    @Operation(summary = "Onlayn müraciətə cavab + tamamla (plant_consultation)")
    public ResponseEntity<ApiResponse<DiagnosisDetailResponse>> replyConsultation(
            @PathVariable Long id,
            @Valid @RequestBody AgronomistReplyRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                diagnosisService.replyConsultation(id, request),
                "Agronom cavabi ugurla yadda saxlanildi."
        ));
    }

    @Deprecated
    @PatchMapping("/agronomist/diagnosis/{id}/reserve")
    @Operation(summary = "Reserve (legacy path) — yalnız ev ziyarəti id")
    public ResponseEntity<ApiResponse<DiagnosisDetailResponse>> reserveByAgronomistLegacy(
            @PathVariable Long id,
            @Valid @RequestBody ReserveDiagnosisRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                diagnosisService.reserveByAgronomist(id, request),
                "Rezervasiya ugurla edildi."
        ));
    }

    @GetMapping("/agronomist/diagnosis/reserved")
    @Operation(summary = "List reserved diagnosis requests by agronomist")
    public ResponseEntity<ApiResponse<List<DiagnosisDetailResponse>>> getReservedDiagnoses(
            @RequestParam Long agronomistId
    ) {
        return ResponseEntity.ok(ApiResponse.success(diagnosisService.getReservedDiagnoses(agronomistId)));
    }

    @Deprecated
    @PatchMapping("/agronomist/diagnosis/{id}/reply")
    @Operation(summary = "Cavab (legacy) — kind default HOME_VISIT")
    public ResponseEntity<ApiResponse<DiagnosisDetailResponse>> replyLegacy(
            @PathVariable Long id,
            @RequestParam(defaultValue = "HOME_VISIT") DiagnosisKind kind,
            @Valid @RequestBody AgronomistReplyRequest request
    ) {
        DiagnosisDetailResponse body =
                kind == DiagnosisKind.HOME_VISIT
                        ? diagnosisService.replyHomeVisit(id, request)
                        : diagnosisService.replyConsultation(id, request);
        return ResponseEntity.ok(ApiResponse.success(body, "Agronom cavabi ugurla yadda saxlanildi."));
    }
}
