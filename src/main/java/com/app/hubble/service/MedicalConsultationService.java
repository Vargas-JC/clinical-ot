package com.app.hubble.service;

import com.app.hubble.dto.request.MedicalConsultationRequest;
import com.app.hubble.dto.response.MedicalConsultationResponse;
import com.app.hubble.entity.MedicalConsultation;
import com.app.hubble.entity.User;
import com.app.hubble.enumeration.UserRole;
import com.app.hubble.exception.BadRequestException;
import com.app.hubble.exception.ForbiddenException;
import com.app.hubble.exception.NotFoundException;
import com.app.hubble.exception.UnauthorizedException;
import com.app.hubble.repository.AppointmentRepository;
import com.app.hubble.repository.MedicalConsultationRepository;
import com.app.hubble.repository.PatientRepository;
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
public class MedicalConsultationService {
    private final MedicalConsultationRepository medicalConsultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public Mono<PageResponse<MedicalConsultationResponse>> findAllMedicalConsultations(int page, int size) {
        int offset = page * size;
        Flux<MedicalConsultationResponse> data = medicalConsultationRepository.findAllActivePaged(size, offset).map(this::toResponse);
        Mono<Long> total = medicalConsultationRepository.countByDeletedAtIsNull();
        return PageUtils.buildPage(data, total, page, size);
    }

    public Mono<PageResponse<MedicalConsultationResponse>> findMedicalConsultationsForCurrentUser(UUID userId, int page, int size) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .filter(u -> u.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new UnauthorizedException("Usuario no encontrado.")))
                .flatMap(user -> {
                    if (user.getRole() == UserRole.ADMIN) {
                        return findAllMedicalConsultations(page, size);
                    }
                    return patientRepository.findByUserIdAndDeletedAtIsNull(userId)
                            .switchIfEmpty(Mono.error(new ForbiddenException("Se requiere perfil de paciente.")))
                            .flatMap(ignored -> pageConsultationsForPatientUser(userId, page, size));
                });
    }

    private Mono<PageResponse<MedicalConsultationResponse>> pageConsultationsForPatientUser(
            UUID userId,
            int page,
            int size) {
        int offset = page * size;
        Flux<MedicalConsultationResponse> data = medicalConsultationRepository
                .findPagedForPatientUser(userId, size, offset)
                .map(this::toResponse);
        Mono<Long> total = medicalConsultationRepository.countActiveForPatientUser(userId);
        return PageUtils.buildPage(data, total, page, size);
    }

    public Mono<MedicalConsultationResponse> findMedicalConsultationById(UUID id) {
        return medicalConsultationRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Consulta médica no encontrada.")))
                .map(this::toResponse);
    }

    public Mono<MedicalConsultationResponse> saveMedicalConsultation(MedicalConsultationRequest body) {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findById(body.getAppointmentId())
                .filter(a -> a.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Cita no encontrada.")))
                .flatMap(a -> {
                    MedicalConsultation row = MedicalConsultation.builder()
                            .appointmentId(body.getAppointmentId())
                            .visualAcuity(blankToNull(body.getVisualAcuity()))
                            .intraocularPressure(blankToNull(body.getIntraocularPressure()))
                            .diagnosis(blankToNull(body.getDiagnosis()))
                            .notes(blankToNull(body.getNotes()))
                            .active(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return medicalConsultationRepository.save(row);
                })
                .map(this::toResponse);
    }

    public Mono<MedicalConsultationResponse> updateMedicalConsultation(MedicalConsultationRequest body) {
        if (body.getId() == null) {
            return Mono.error(new BadRequestException("Id de consulta es nulo."));
        }
        LocalDateTime now = LocalDateTime.now();
        return medicalConsultationRepository.findById(body.getId())
                .filter(c -> c.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Consulta médica no encontrada.")))
                .flatMap(row -> appointmentRepository.findById(body.getAppointmentId())
                        .filter(a -> a.getDeletedAt() == null)
                        .switchIfEmpty(Mono.error(new NotFoundException("Cita no encontrada.")))
                        .flatMap(a -> {
                            row.setAppointmentId(body.getAppointmentId());
                            row.setVisualAcuity(blankToNull(body.getVisualAcuity()));
                            row.setIntraocularPressure(blankToNull(body.getIntraocularPressure()));
                            row.setDiagnosis(blankToNull(body.getDiagnosis()));
                            row.setNotes(blankToNull(body.getNotes()));
                            row.setActive(body.isActive());
                            row.setUpdatedAt(now);
                            return medicalConsultationRepository.save(row);
                        }))
                .map(this::toResponse);
    }

    public Mono<Void> deleteMedicalConsultation(UUID id) {
        LocalDateTime now = LocalDateTime.now();
        return medicalConsultationRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Consulta médica no encontrada.")))
                .flatMap(row -> {
                    row.setDeletedAt(now);
                    row.setUpdatedAt(now);
                    row.setActive(false);
                    return medicalConsultationRepository.save(row);
                })
                .then();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private MedicalConsultationResponse toResponse(MedicalConsultation c) {
        return MedicalConsultationResponse.builder()
                .id(c.getId())
                .appointmentId(c.getAppointmentId())
                .visualAcuity(c.getVisualAcuity() == null ? "" : c.getVisualAcuity())
                .intraocularPressure(c.getIntraocularPressure() == null ? "" : c.getIntraocularPressure())
                .diagnosis(c.getDiagnosis() == null ? "" : c.getDiagnosis())
                .notes(c.getNotes() == null ? "" : c.getNotes())
                .active(c.isActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
