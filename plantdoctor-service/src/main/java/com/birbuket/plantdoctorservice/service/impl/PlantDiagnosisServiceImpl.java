package com.birbuket.plantdoctorservice.service.impl;

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
import com.birbuket.plantdoctorservice.enums.DiagnosisStatus;
import com.birbuket.plantdoctorservice.enums.PlantCountRange;
import com.birbuket.plantdoctorservice.enums.VisitTimeSlot;
import com.birbuket.plantdoctorservice.mail.AgronomistReplyMailSender;
import com.birbuket.plantdoctorservice.models.HomeVisitBooking;
import com.birbuket.plantdoctorservice.models.PlantConsultation;
import com.birbuket.plantdoctorservice.models.PlantVisitAddress;
import com.birbuket.plantdoctorservice.repository.HomeVisitRepository;
import com.birbuket.plantdoctorservice.repository.PlantConsultationRepository;
import com.birbuket.plantdoctorservice.repository.PlantVisitAddressRepository;
import com.birbuket.plantdoctorservice.service.PlantDiagnosisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlantDiagnosisServiceImpl implements PlantDiagnosisService {

    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
    private static final int AGRONOMIST_RESPONSE_MAX_LENGTH = 3900;

    /** Sadə sintaks — əlavə doğrultma front tərəfdə JWT/email ilə bağlıdır */
    private static final Pattern CONTACT_EMAIL_SIMPLE = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final HomeVisitRepository homeVisitRepository;
    private final PlantConsultationRepository plantConsultationRepository;
    private final PlantVisitAddressRepository addressRepository;
    private final RestTemplateBuilder restTemplateBuilder;
    private final AgronomistReplyMailSender agronomistReplyMailSender;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    @Value("${app.gateway-base-url:http://localhost:8081}")
    private String gatewayBaseUrl;
    @Value("${app.auth-service.url:http://localhost:8082}")
    private String authServiceUrl;
    @Value("${app.visit-pricing.base-visit-fee:15.00}")
    private BigDecimal baseVisitFee;
    @Value("${app.visit-pricing.free-distance-km:5}")
    private BigDecimal freeDistanceKm;
    @Value("${app.visit-pricing.per-km-fee:0.60}")
    private BigDecimal perKmFee;
    @Value("${app.visit-pricing.office-latitude:}")
    private String officeLatStr;
    @Value("${app.visit-pricing.office-longitude:}")
    private String officeLonStr;
    @Value("${app.visit-pricing.office-address:}")
    private String officeAddress;
    @Value("${app.visit-pricing.office-name:BirBuket Flower}")
    private String officeName;

    @Override
    public CreateDiagnosisResponse createDiagnosis(CreateDiagnosisRequest request) {
        validateInput(request);
        String customerEmail = effectiveCustomerEmail(request.getUserId(), request.getContactEmail());
        PlantVisitAddress selectedAddress = resolveOrCreateAddress(request);
        BigDecimal distanceKm = resolveDistanceKm(request, selectedAddress);
        BigDecimal plantCountFee = request.getPlantCountRange().fee();
        BigDecimal transportFee = calculateTransportFee(distanceKm);
        BigDecimal totalFee = baseVisitFee.add(plantCountFee).add(transportFee).setScale(2, RoundingMode.HALF_UP);

        HomeVisitBooking row = HomeVisitBooking.builder()
                .plantType(request.getPlantType().trim())
                .email(customerEmail)
                .symptoms(request.getSymptoms().trim())
                .imagePath("")
                .imageUrl("")
                .addressId(selectedAddress.getId())
                .phoneNumber(selectedAddress.getPhoneNumber())
                .fullAddressLine(selectedAddress.getFullAddressLine())
                .specialNote(trimToMaxLength(request.getSpecialNote(), 1000))
                .plantCountRange(request.getPlantCountRange())
                .visitDate(request.getVisitDate())
                .visitTimeSlot(request.getVisitTimeSlot())
                .customerLatitude(selectedAddress.getLatitude())
                .customerLongitude(selectedAddress.getLongitude())
                .distanceKm(distanceKm)
                .baseVisitFee(baseVisitFee.setScale(2, RoundingMode.HALF_UP))
                .plantCountFee(plantCountFee.setScale(2, RoundingMode.HALF_UP))
                .transportFee(transportFee)
                .totalFee(totalFee)
                .status(DiagnosisStatus.PENDING)
                .build();

        HomeVisitBooking saved = homeVisitRepository.saveAndFlush(row);
        return CreateDiagnosisResponse.builder()
                .id(saved.getId())
                .kind(DiagnosisKind.HOME_VISIT)
                .status(saved.getStatus())
                .imageUrl(saved.getImageUrl())
                .diagnosisLink(normalizeBaseUrl(gatewayBaseUrl) + "/api/plantdoctor/home-visit/" + saved.getId())
                .emailSent(false)
                .plantCountRange(saved.getPlantCountRange())
                .visitDate(saved.getVisitDate())
                .visitTimeSlot(saved.getVisitTimeSlot())
                .baseVisitFee(saved.getBaseVisitFee())
                .plantCountFee(saved.getPlantCountFee())
                .transportFee(saved.getTransportFee())
                .totalFee(saved.getTotalFee())
                .build();
    }

    @Override
    public CreateDiagnosisResponse createConsultation(CreateConsultationRequest request) {
        validateConsultationInput(request);
        String customerEmail = effectiveCustomerEmail(request.getUserId(), request.getContactEmail());
        Path savedFile = saveImage(request.getImage());
        String relative = "/uploads/plantdoctor/" + savedFile.getFileName();
        String imageUrl = normalizeBaseUrl(gatewayBaseUrl) + relative;

        PlantConsultation row = PlantConsultation.builder()
                .plantType(request.getPlantType().trim())
                .email(customerEmail)
                .symptoms(request.getSymptoms().trim())
                .imagePath(savedFile.toString())
                .imageUrl(imageUrl)
                .specialNote(trimToMaxLength(request.getSpecialNote(), 1000))
                .status(DiagnosisStatus.PENDING)
                .build();

        PlantConsultation saved = plantConsultationRepository.saveAndFlush(row);
        return CreateDiagnosisResponse.builder()
                .id(saved.getId())
                .kind(DiagnosisKind.CONSULTATION)
                .status(saved.getStatus())
                .imageUrl(saved.getImageUrl())
                .diagnosisLink(normalizeBaseUrl(gatewayBaseUrl) + "/api/plantdoctor/consultation/" + saved.getId())
                .emailSent(false)
                .plantCountRange(null)
                .visitDate(null)
                .visitTimeSlot(null)
                .baseVisitFee(null)
                .plantCountFee(null)
                .transportFee(null)
                .totalFee(null)
                .build();
    }

    /**
     * contactEmail dolu ikən əsas ünvandır — rezerv/qəbul məktubları real poçta düşsün (auth xətasından
     * asılı olmayaraq).
     */
    private String effectiveCustomerEmail(Long userId, String contactEmail) {
        if (!isBlank(contactEmail)) {
            String t = contactEmail.trim();
            if (t.length() > 160) {
                throw new IllegalArgumentException("contactEmail is too long");
            }
            if (!CONTACT_EMAIL_SIMPLE.matcher(t).matches()) {
                throw new IllegalArgumentException("contactEmail has invalid format");
            }
            return t;
        }
        return resolveUserEmailSafe(userId);
    }

    private String resolveUserEmailSafe(Long userId) {
        try {
            return resolveUserEmail(userId);
        } catch (Exception ex) {
            log.warn("User email could not be resolved for userId={}, using fallback email", userId);
            return "unknown@birbuket.local";
        }
    }

    private static boolean isPlaceholderEmail(String email) {
        return email == null || email.isBlank() || email.trim().toLowerCase(Locale.ROOT).endsWith("@birbuket.local");
    }

    private static String briefMaskEmail(String e) {
        if (e == null || e.isBlank()) {
            return "(boş)";
        }
        int at = e.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return e.charAt(0) + "***" + e.substring(at);
    }

    @Override
    @Transactional(readOnly = true)
    public DiagnosisDetailResponse getHomeVisit(Long id) {
        HomeVisitBooking row =
                homeVisitRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Home visit not found"));
        return mapHomeVisit(row);
    }

    @Override
    @Transactional(readOnly = true)
    public DiagnosisDetailResponse getConsultation(Long id) {
        PlantConsultation row = plantConsultationRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consultation not found"));
        return mapConsultation(row);
    }

    @Override
    @Transactional(readOnly = true)
    public DiagnosisDetailResponse getDiagnosis(Long id, DiagnosisKind kind) {
        if (kind == DiagnosisKind.HOME_VISIT) {
            return getHomeVisit(id);
        }
        if (kind == DiagnosisKind.CONSULTATION) {
            return getConsultation(id);
        }
        throw new IllegalArgumentException("kind must be HOME_VISIT or CONSULTATION");
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisDetailResponse> getPendingDiagnoses() {
        Stream<DiagnosisDetailResponse> home =
                homeVisitRepository.findAllByStatusOrderByCreatedAtDesc(DiagnosisStatus.PENDING).stream()
                        .map(this::mapHomeVisit);
        Stream<DiagnosisDetailResponse> cons =
                plantConsultationRepository.findAllByStatusOrderByCreatedAtDesc(DiagnosisStatus.PENDING).stream()
                        .map(this::mapConsultation);
        return mergeByCreatedAtDesc(Stream.concat(home, cons));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisDetailResponse> getPendingHomeVisits() {
        return homeVisitRepository.findAllByStatusOrderByCreatedAtDesc(DiagnosisStatus.PENDING).stream()
                .map(this::mapHomeVisit)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisDetailResponse> getPendingConsultations() {
        return plantConsultationRepository.findAllByStatusOrderByCreatedAtDesc(DiagnosisStatus.PENDING).stream()
                .map(this::mapConsultation)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId is required");
        }
        return addressRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapAddress)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StoreLocationResponse getStoreLocation() {
        return StoreLocationResponse.builder()
                .name(officeName)
                .addressLine(officeAddress)
                .latitude(parseDoubleOrNull(officeLatStr))
                .longitude(parseDoubleOrNull(officeLonStr))
                .build();
    }

    @Override
    @Transactional
    public AddressResponse createAddress(CreateAddressRequest request) {
        if (request == null || request.getUserId() == null || request.getUserId() <= 0) {
            throw new IllegalArgumentException("userId is required");
        }
        if (isBlank(request.getPhoneNumber())) {
            throw new IllegalArgumentException("phoneNumber is required");
        }
        if (isBlank(request.getFullAddressLine())) {
            throw new IllegalArgumentException("fullAddressLine is required");
        }
        PlantVisitAddress saved = addressRepository.save(
                PlantVisitAddress.builder()
                        .userId(request.getUserId())
                        .phoneNumber(request.getPhoneNumber().trim())
                        .fullAddressLine(request.getFullAddressLine().trim())
                        .latitude(request.getLatitude())
                        .longitude(request.getLongitude())
                        .build());
        return mapAddress(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisDetailResponse> getReservedDiagnoses(Long agronomistId) {
        if (agronomistId == null || agronomistId <= 0) {
            throw new IllegalArgumentException("agronomistId is required");
        }
        return homeVisitRepository
                .findAllByStatusAndAgronomistIdOrderByCreatedAtDesc(DiagnosisStatus.RESERVED, agronomistId)
                .stream()
                .map(this::mapHomeVisit)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisDetailResponse> getAllDiagnoses() {
        Stream<DiagnosisDetailResponse> home =
                homeVisitRepository.findAllByOrderByCreatedAtDesc().stream().map(this::mapHomeVisit);
        Stream<DiagnosisDetailResponse> cons =
                plantConsultationRepository.findAllByOrderByCreatedAtDesc().stream().map(this::mapConsultation);
        return mergeByCreatedAtDesc(Stream.concat(home, cons));
    }

    @Override
    @Transactional
    public DiagnosisDetailResponse reserveByAgronomist(Long id, ReserveDiagnosisRequest request) {
        HomeVisitBooking row = homeVisitRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Home visit not found"));
        if (request == null || request.getAgronomistId() == null || request.getAgronomistId() <= 0) {
            throw new IllegalArgumentException("agronomistId is required");
        }
        if (row.getStatus() == DiagnosisStatus.COMPLETED) {
            throw new IllegalStateException("Completed diagnosis cannot be reserved");
        }
        if (row.getStatus() == DiagnosisStatus.RESERVED
                && row.getAgronomistId() != null
                && !row.getAgronomistId().equals(request.getAgronomistId())) {
            throw new IllegalStateException("Diagnosis already reserved by another agronomist");
        }
        if (row.getStatus() == DiagnosisStatus.PENDING) {
            row.setStatus(DiagnosisStatus.RESERVED);
            row.setAgronomistId(request.getAgronomistId());
            row.setReservedAt(LocalDateTime.now());
            homeVisitRepository.save(row);
            scheduleHomeVisitReservedEmail(
                    row.getEmail(),
                    row.getId(),
                    row.getVisitDate(),
                    row.getVisitTimeSlot(),
                    row.getFullAddressLine());
        }
        return mapHomeVisit(row);
    }

    @Override
    @Transactional
    public DiagnosisDetailResponse reserveConsultationByAgronomist(Long id, ReserveDiagnosisRequest request) {
        PlantConsultation row = plantConsultationRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consultation not found"));
        if (request == null || request.getAgronomistId() == null || request.getAgronomistId() <= 0) {
            throw new IllegalArgumentException("agronomistId is required");
        }
        if (row.getStatus() == DiagnosisStatus.COMPLETED) {
            throw new IllegalStateException("Tamamlanmış müraciət rezerv olunmur");
        }
        if (row.getStatus() == DiagnosisStatus.RESERVED
                && row.getAgronomistId() != null
                && !row.getAgronomistId().equals(request.getAgronomistId())) {
            throw new IllegalStateException("Bu müraciəti başqa aqronom qəbul edib");
        }
        if (row.getStatus() == DiagnosisStatus.PENDING) {
            row.setStatus(DiagnosisStatus.RESERVED);
            row.setAgronomistId(request.getAgronomistId());
            row.setReservedAt(LocalDateTime.now());
            plantConsultationRepository.save(row);
            scheduleConsultationReservedEmail(row.getEmail(), row.getId(), row.getPlantType(), row.getSymptoms());
        }
        return mapConsultation(row);
    }

    @Override
    @Transactional
    public DiagnosisDetailResponse replyHomeVisit(Long id, AgronomistReplyRequest request) {
        HomeVisitBooking row =
                homeVisitRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Home visit not found"));
        validateReplyRequest(request);

        if (row.getStatus() == DiagnosisStatus.COMPLETED) {
            Long rid = row.getAgronomistId();
            if (rid == null || rid.equals(request.getAgronomistId())) {
                return mapHomeVisit(row);
            }
            throw new IllegalStateException(
                    "Bu rezerv artıq yekunlaşdırılıb. Cavab yeniləmək mümkün deyil (və ya başqa aqronom ID ilə cəhd).");
        }

        /** PENDING + reply: rezerv məktubu + eyni sorğuda yekun (front ayrıca /reserve etməyə məcbur deyil). */
        if (row.getStatus() == DiagnosisStatus.PENDING) {
            row.setStatus(DiagnosisStatus.RESERVED);
            row.setAgronomistId(request.getAgronomistId());
            row.setReservedAt(LocalDateTime.now());
            homeVisitRepository.save(row);
            scheduleHomeVisitReservedEmail(
                    row.getEmail(),
                    row.getId(),
                    row.getVisitDate(),
                    row.getVisitTimeSlot(),
                    row.getFullAddressLine());
        }
        if (row.getStatus() == DiagnosisStatus.FAILED) {
            throw new IllegalStateException("Failed diagnosis cannot be replied");
        }
        if (row.getStatus() != DiagnosisStatus.RESERVED) {
            throw new IllegalStateException("Ev ziyarətinə yalnız RESERVED statusundan sonra cavab yazıla bilər");
        }
        validateReservedMatches(row.getAgronomistId(), request.getAgronomistId());
        row.setAgronomistResponse(trimToMaxLength(request.getResponse().trim(), AGRONOMIST_RESPONSE_MAX_LENGTH));
        row.setStatus(DiagnosisStatus.COMPLETED);
        row.setCompletedAt(LocalDateTime.now());
        HomeVisitBooking saved = homeVisitRepository.save(row);
        scheduleAgronomistReplyEmail(
                saved.getEmail(),
                saved.getId(),
                DiagnosisKind.HOME_VISIT,
                saved.getAgronomistResponse(),
                saved.getPlantType());
        return mapHomeVisit(saved);
    }

    @Override
    @Transactional
    public DiagnosisDetailResponse replyConsultation(Long id, AgronomistReplyRequest request) {
        PlantConsultation row = plantConsultationRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consultation not found"));
        validateReplyRequest(request);

        if (row.getStatus() == DiagnosisStatus.COMPLETED) {
            Long rid = row.getAgronomistId();
            if (rid == null || rid.equals(request.getAgronomistId())) {
                return mapConsultation(row);
            }
            throw new IllegalStateException(
                    "Bu müraciət artıq yekunlaşdırılıb; başqa aqronom kimi cavab göndərilə bilməz.");
        }

        applyPreReplyStateTransition(row.getStatus(), row.getAgronomistId(),
                () -> {
                    row.setStatus(DiagnosisStatus.RESERVED);
                    row.setAgronomistId(request.getAgronomistId());
                    row.setReservedAt(LocalDateTime.now());
                    plantConsultationRepository.save(row);
                },
                reservedAgronomistId -> validateReservedMatches(reservedAgronomistId, request.getAgronomistId()));
        row.setAgronomistResponse(trimToMaxLength(request.getResponse().trim(), AGRONOMIST_RESPONSE_MAX_LENGTH));
        row.setStatus(DiagnosisStatus.COMPLETED);
        row.setCompletedAt(LocalDateTime.now());
        PlantConsultation saved = plantConsultationRepository.save(row);
        scheduleAgronomistReplyEmail(
                saved.getEmail(),
                saved.getId(),
                DiagnosisKind.CONSULTATION,
                saved.getAgronomistResponse(),
                saved.getPlantType());
        return mapConsultation(saved);
    }

    private void validateReplyRequest(AgronomistReplyRequest request) {
        if (request == null || isBlank(request.getResponse())) {
            throw new IllegalArgumentException("response is required");
        }
        if (request.getAgronomistId() == null || request.getAgronomistId() <= 0) {
            throw new IllegalArgumentException("agronomistId is required");
        }
    }

    private void validateReservedMatches(Long reservedAgronomistId, Long requestAgronomistId) {
        if (reservedAgronomistId != null && !reservedAgronomistId.equals(requestAgronomistId)) {
            throw new IllegalStateException("Diagnosis is reserved by another agronomist");
        }
    }

    private void applyPreReplyStateTransition(
            DiagnosisStatus status,
            Long currentAgronomistId,
            Runnable onPendingReserve,
            java.util.function.Consumer<Long> validateReserved
    ) {
        if (status == DiagnosisStatus.COMPLETED) {
            throw new IllegalStateException("Completed diagnosis cannot be replied");
        }
        if (status == DiagnosisStatus.FAILED) {
            throw new IllegalStateException("Failed diagnosis cannot be replied");
        }
        if (status == DiagnosisStatus.PENDING) {
            onPendingReserve.run();
        } else if (status == DiagnosisStatus.RESERVED) {
            validateReserved.accept(currentAgronomistId);
            if (currentAgronomistId == null) {
                onPendingReserve.run();
            }
        } else {
            throw new IllegalStateException("Diagnosis must be pending or reserved before reply");
        }
    }

    private void scheduleHomeVisitReservedEmail(
            String email,
            Long homeVisitId,
            LocalDate visitDate,
            VisitTimeSlot visitTimeSlot,
            String addressLine
    ) {
        if (isPlaceholderEmail(email)) {
            log.warn(
                    "HOME_VISIT reserved məktubu göndərilmir: düzgün e-poçt yoxdur (contactEmail və ya auth). id={}, saxlanmış={}",
                    homeVisitId,
                    email);
            return;
        }
        log.info(
                "HOME_VISIT reserved qəbul məktubu trx commit sonrası göndəriləcək id={}, to≈{}",
                homeVisitId,
                briefMaskEmail(email));
        Runnable dispatch = () ->
                agronomistReplyMailSender.sendHomeVisitReservedConfirmation(
                        email,
                        homeVisitId,
                        visitDate,
                        visitTimeSlot,
                        addressLine);
        runAfterTransactionalCommit(
                dispatch,
                "HOME_VISIT RESERVED SMTP id=" + homeVisitId + " to=" + briefMaskEmail(email));
    }

    private void scheduleConsultationReservedEmail(
            String email, Long consultationId, String plantType, String symptoms) {
        if (isPlaceholderEmail(email)) {
            log.warn(
                    "CONSULTATION reserved məktubu göndərilmir: düzgün e-poçt yoxdur. id={}, saxlanmış={}",
                    consultationId,
                    email);
            return;
        }
        log.info(
                "CONSULTATION reserved qəbul məktubu trx commit sonrası göndəriləcək id={}, to≈{}",
                consultationId,
                briefMaskEmail(email));
        Runnable dispatch =
                () ->
                        agronomistReplyMailSender.sendConsultationReservedConfirmation(
                                email, consultationId, plantType, symptoms);
        runAfterTransactionalCommit(
                dispatch, "CONSULTATION RESERVED SMTP id=" + consultationId + " to=" + briefMaskEmail(email));
    }

    private void scheduleAgronomistReplyEmail(
            String email, Long id, DiagnosisKind kind, String agronomistResponse, String plantType) {
        Runnable dispatch =
                () -> agronomistReplyMailSender.send(email, id, kind, agronomistResponse, plantType);
        runAfterTransactionalCommit(dispatch, "agronom reply SMTP " + kind + " id=" + id);
    }

    private void runAfterTransactionalCommit(Runnable dispatch, String logLabel) {
        Runnable safe =
                () -> {
                    try {
                        dispatch.run();
                    } catch (Exception ex) {
                        log.error("afterCommit [{}] poçt göndəricisi uğursuz (HTTP cavabı təsir olunmur)", logLabel, ex);
                    }
                };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("afterCommit [{}]", logLabel);
                    safe.run();
                }
            });
        } else {
            log.warn("SMTP: aktiv trx sinxronizasiya yoxdur — göndəriş birbaşa [{}]", logLabel);
            safe.run();
        }
    }

    private static List<DiagnosisDetailResponse> mergeByCreatedAtDesc(Stream<DiagnosisDetailResponse> combined) {
        return combined.sorted(Comparator.comparing(DiagnosisDetailResponse::getCreatedAt).reversed()).toList();
    }

    private DiagnosisDetailResponse mapHomeVisit(HomeVisitBooking row) {
        return DiagnosisDetailResponse.builder()
                .id(row.getId())
                .kind(DiagnosisKind.HOME_VISIT)
                .plantType(row.getPlantType())
                .email(row.getEmail())
                .symptoms(row.getSymptoms())
                .imageUrl(row.getImageUrl())
                .phoneNumber(row.getPhoneNumber())
                .fullAddressLine(row.getFullAddressLine())
                .specialNote(row.getSpecialNote())
                .agronomistId(row.getAgronomistId())
                .plantCountRange(row.getPlantCountRange())
                .visitDate(row.getVisitDate())
                .visitTimeSlot(row.getVisitTimeSlot())
                .distanceKm(row.getDistanceKm())
                .baseVisitFee(row.getBaseVisitFee())
                .plantCountFee(row.getPlantCountFee())
                .transportFee(row.getTransportFee())
                .totalFee(row.getTotalFee())
                .status(row.getStatus())
                .agronomistResponse(row.getAgronomistResponse())
                .reservedAt(row.getReservedAt())
                .completedAt(row.getCompletedAt())
                .createdAt(row.getCreatedAt())
                .build();
    }

    private DiagnosisDetailResponse mapConsultation(PlantConsultation row) {
        return DiagnosisDetailResponse.builder()
                .id(row.getId())
                .kind(DiagnosisKind.CONSULTATION)
                .plantType(row.getPlantType())
                .email(row.getEmail())
                .symptoms(row.getSymptoms())
                .imageUrl(row.getImageUrl())
                .phoneNumber(null)
                .fullAddressLine(null)
                .specialNote(row.getSpecialNote())
                .agronomistId(row.getAgronomistId())
                .plantCountRange(null)
                .visitDate(null)
                .visitTimeSlot(null)
                .distanceKm(null)
                .baseVisitFee(null)
                .plantCountFee(null)
                .transportFee(null)
                .totalFee(null)
                .status(row.getStatus())
                .agronomistResponse(row.getAgronomistResponse())
                .reservedAt(row.getReservedAt())
                .completedAt(row.getCompletedAt())
                .createdAt(row.getCreatedAt())
                .build();
    }

    private void validateInput(CreateDiagnosisRequest request) {
        if (request == null) throw new IllegalArgumentException("request is required");
        if (request.getUserId() == null || request.getUserId() <= 0)
            throw new IllegalArgumentException("userId is required");
        if (isBlank(request.getPlantType())) throw new IllegalArgumentException("plantType is required");
        if (isBlank(request.getSymptoms())) throw new IllegalArgumentException("symptoms is required");
        if (request.getPlantCountRange() == null)
            throw new IllegalArgumentException("plantCountRange is required");
        if (request.getVisitDate() == null) throw new IllegalArgumentException("visitDate is required");
        if (request.getVisitDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("visitDate cannot be in the past");
        if (request.getVisitTimeSlot() == null) throw new IllegalArgumentException("visitTimeSlot is required");
    }

    private void validateConsultationInput(CreateConsultationRequest request) {
        if (request == null) throw new IllegalArgumentException("request is required");
        if (request.getUserId() == null || request.getUserId() <= 0)
            throw new IllegalArgumentException("userId is required");
        if (isBlank(request.getPlantType())) throw new IllegalArgumentException("plantType is required");
        if (isBlank(request.getSymptoms())) throw new IllegalArgumentException("symptoms is required");
        MultipartFile image = request.getImage();
        if (image == null || image.isEmpty()) throw new IllegalArgumentException("image is required");
        if (image.getSize() > MAX_IMAGE_SIZE_BYTES)
            throw new IllegalArgumentException("image size must be <= 5MB");
        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("only JPG and PNG images are allowed");
        }
    }

    private PlantVisitAddress resolveOrCreateAddress(CreateDiagnosisRequest request) {
        if (request.getAddressId() != null) {
            PlantVisitAddress address = addressRepository
                    .findById(request.getAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("address not found"));
            if (!address.getUserId().equals(request.getUserId())) {
                throw new IllegalArgumentException("address does not belong to user");
            }
            if (!isBlank(request.getPhoneNumber()) || !isBlank(request.getFullAddressLine())) {
                address.setPhoneNumber(
                        request.getPhoneNumber() == null ? address.getPhoneNumber() : request.getPhoneNumber().trim());
                address.setFullAddressLine(
                        request.getFullAddressLine() == null
                                ? address.getFullAddressLine()
                                : request.getFullAddressLine().trim());
                if (request.getLatitude() != null) address.setLatitude(request.getLatitude());
                if (request.getLongitude() != null) address.setLongitude(request.getLongitude());
                return addressRepository.save(address);
            }
            return address;
        }

        if (isBlank(request.getPhoneNumber()) || isBlank(request.getFullAddressLine())) {
            throw new IllegalArgumentException("Either addressId or phoneNumber + fullAddressLine is required");
        }
        PlantVisitAddress created = PlantVisitAddress.builder()
                .userId(request.getUserId())
                .phoneNumber(request.getPhoneNumber().trim())
                .fullAddressLine(request.getFullAddressLine().trim())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
        if (request.isSaveAddress()) {
            return addressRepository.save(created);
        }
        created.setCreatedAt(LocalDateTime.now());
        return created;
    }

    private BigDecimal resolveDistanceKm(CreateDiagnosisRequest request, PlantVisitAddress address) {
        if (request.getDistanceKm() != null) {
            if (request.getDistanceKm() < 0) throw new IllegalArgumentException("distanceKm must be >= 0");
            return BigDecimal.valueOf(request.getDistanceKm()).setScale(2, RoundingMode.HALF_UP);
        }
        Double officeLat = parseDoubleOrNull(officeLatStr);
        Double officeLon = parseDoubleOrNull(officeLonStr);
        if (officeLat != null && officeLon != null && address.getLatitude() != null && address.getLongitude() != null) {
            return haversineKm(officeLat, officeLon, address.getLatitude(), address.getLongitude());
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTransportFee(BigDecimal distanceKm) {
        BigDecimal chargedDistance = distanceKm.subtract(freeDistanceKm);
        if (chargedDistance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return chargedDistance.multiply(perKmFee).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                                * Math.cos(Math.toRadians(lat2))
                                * Math.sin(dLon / 2)
                                * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(earthRadiusKm * c).setScale(2, RoundingMode.HALF_UP);
    }

    private Double parseDoubleOrNull(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String resolveUserEmail(Long userId) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        String url = normalizeBaseUrl(authServiceUrl) + "/auth/users/" + userId;
        Map response;
        try {
            response = restTemplate.getForObject(url, Map.class);
        } catch (HttpStatusCodeException e) {
            throw new IllegalArgumentException("User not found for userId=" + userId);
        } catch (Exception e) {
            throw new IllegalStateException("Auth service is unavailable. Please try again.", e);
        }
        if (response == null) {
            throw new IllegalArgumentException("User not found");
        }
        Object dataObj = response.get("data");
        if (!(dataObj instanceof Map<?, ?> dataMap)) {
            throw new IllegalArgumentException("User not found");
        }
        Object emailObj = dataMap.get("email");
        if (!(emailObj instanceof String email) || isBlank(email)) {
            throw new IllegalArgumentException("User email not found");
        }
        return email.trim();
    }

    private Path saveImage(MultipartFile image) {
        try {
            Path dir = Path.of(uploadDir, "plantdoctor").toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String extension = imageExtension(image.getOriginalFilename(), image.getContentType());
            String fileName = UUID.randomUUID() + extension;
            Path target = dir.resolve(fileName);
            Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save image", e);
        }
    }

    private static String imageExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png")) {
                return ext.equals(".jpeg") ? ".jpg" : ext;
            }
        }
        return "image/png".equalsIgnoreCase(contentType) ? ".png" : ".jpg";
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private AddressResponse mapAddress(PlantVisitAddress address) {
        return AddressResponse.builder()
                .id(address.getId())
                .userId(address.getUserId())
                .phoneNumber(address.getPhoneNumber())
                .fullAddressLine(address.getFullAddressLine())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .createdAt(address.getCreatedAt())
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToMaxLength(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
