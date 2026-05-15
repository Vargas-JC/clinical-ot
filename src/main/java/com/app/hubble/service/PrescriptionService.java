package com.app.hubble.service;

import com.app.hubble.dto.request.PrescriptionRequest;
import com.app.hubble.dto.response.PrescriptionResponse;
import com.app.hubble.entity.Prescription;
import com.app.hubble.entity.User;
import com.app.hubble.enumeration.UserRole;
import com.app.hubble.exception.BadRequestException;
import com.app.hubble.exception.ForbiddenException;
import com.app.hubble.exception.NotFoundException;
import com.app.hubble.exception.UnauthorizedException;
import com.app.hubble.repository.MedicalConsultationRepository;
import com.app.hubble.repository.PatientRepository;
import com.app.hubble.repository.PrescriptionRepository;
import com.app.hubble.repository.UserRepository;
import com.app.hubble.util.PageResponse;
import com.app.hubble.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalConsultationRepository medicalConsultationRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public Mono<PageResponse<PrescriptionResponse>> findAllPrescriptions(int page, int size) {
        int offset = page * size;
        Flux<PrescriptionResponse> data = prescriptionRepository.findAllActivePaged(size, offset).map(this::toResponse);
        Mono<Long> total = prescriptionRepository.countByDeletedAtIsNull();
        return PageUtils.buildPage(data, total, page, size);
    }

    public Mono<PageResponse<PrescriptionResponse>> findPrescriptionsForCurrentUser(UUID userId, int page, int size) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .filter(u -> u.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new UnauthorizedException("Usuario no encontrado.")))
                .flatMap(user -> {
                    if (user.getRole() == UserRole.ADMIN) {
                        return findAllPrescriptions(page, size);
                    }
                    return patientRepository.findByUserIdAndDeletedAtIsNull(userId)
                            .switchIfEmpty(Mono.error(new ForbiddenException("Se requiere perfil de paciente.")))
                            .flatMap(ignored -> pagePrescriptionsForPatientUser(userId, page, size));
                });
    }

    private Mono<PageResponse<PrescriptionResponse>> pagePrescriptionsForPatientUser(UUID userId, int page, int size) {
        int offset = page * size;
        Flux<PrescriptionResponse> data = prescriptionRepository.findPagedForPatientUser(userId, size, offset)
                .map(this::toResponse);
        Mono<Long> total = prescriptionRepository.countActiveForPatientUser(userId);
        return PageUtils.buildPage(data, total, page, size);
    }

    public Mono<PrescriptionResponse> findPrescriptionById(UUID id) {
        return prescriptionRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Receta no encontrada.")))
                .map(this::toResponse);
    }

    public Mono<PrescriptionResponse> savePrescription(PrescriptionRequest body) {
        LocalDateTime now = LocalDateTime.now();
        return medicalConsultationRepository.findById(body.getConsultationId())
                .filter(c -> c.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Consulta médica no encontrada.")))
                .flatMap(c -> {
                    Prescription row = Prescription.builder()
                            .consultationId(body.getConsultationId())
                            .description(blankToNull(body.getDescription()))
                            .lensType(blankToNull(body.getLensType()))
                            .recommendations(blankToNull(body.getRecommendations()))
                            .active(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return prescriptionRepository.save(row);
                })
                .map(this::toResponse);
    }

    public Mono<PrescriptionResponse> updatePrescription(PrescriptionRequest body) {
        if (body.getId() == null) {
            return Mono.error(new BadRequestException("Id de receta es nulo."));
        }
        LocalDateTime now = LocalDateTime.now();
        return prescriptionRepository.findById(body.getId())
                .filter(p -> p.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Receta no encontrada.")))
                .flatMap(row -> medicalConsultationRepository.findById(body.getConsultationId())
                        .filter(c -> c.getDeletedAt() == null)
                        .switchIfEmpty(Mono.error(new NotFoundException("Consulta médica no encontrada.")))
                        .flatMap(c -> {
                            row.setConsultationId(body.getConsultationId());
                            row.setDescription(blankToNull(body.getDescription()));
                            row.setLensType(blankToNull(body.getLensType()));
                            row.setRecommendations(blankToNull(body.getRecommendations()));
                            row.setActive(body.isActive());
                            row.setUpdatedAt(now);
                            return prescriptionRepository.save(row);
                        }))
                .map(this::toResponse);
    }

    public Mono<Void> deletePrescription(UUID id) {
        LocalDateTime now = LocalDateTime.now();
        return prescriptionRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Receta no encontrada.")))
                .flatMap(row -> {
                    row.setDeletedAt(now);
                    row.setUpdatedAt(now);
                    row.setActive(false);
                    return prescriptionRepository.save(row);
                })
                .then();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private PrescriptionResponse toResponse(Prescription p) {
        return PrescriptionResponse.builder()
                .id(p.getId())
                .consultationId(p.getConsultationId())
                .description(p.getDescription() == null ? "" : p.getDescription())
                .lensType(p.getLensType() == null ? "" : p.getLensType())
                .recommendations(p.getRecommendations() == null ? "" : p.getRecommendations())
                .active(p.isActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
