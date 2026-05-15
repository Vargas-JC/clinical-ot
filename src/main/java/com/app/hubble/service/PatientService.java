package com.app.hubble.service;

import com.app.hubble.dto.request.PatientRequest;
import com.app.hubble.dto.response.PatientResponse;
import com.app.hubble.entity.Patient;
import com.app.hubble.entity.User;
import com.app.hubble.exception.BadRequestException;
import com.app.hubble.exception.ConflictException;
import com.app.hubble.exception.NotFoundException;
import com.app.hubble.mapper.PatientMapper;
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
public class PatientService {
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientMapper patientMapper;

    public Mono<PageResponse<PatientResponse>> findAllPatients(int page, int size) {
        int offset = page * size;
        Flux<PatientResponse> data = patientRepository.findAllPagedDetail(size, offset)
                .map(patientMapper::toResponse);
        Mono<Long> total = patientRepository.countByDeletedAtIsNull();
        return PageUtils.buildPage(data, total, page, size);
    }

    public Mono<PatientResponse> findPatientById(UUID id) {
        return patientRepository.findDetailById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Paciente no encontrado.")))
                .map(patientMapper::toResponse);
    }

    public Mono<PatientResponse> findPatientProfileForCurrentUser(UUID userId) {
        return patientRepository.findByUserIdAndDeletedAtIsNull(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("No tiene ficha de paciente.")))
                .flatMap(p -> patientRepository.findDetailById(p.getId()).map(patientMapper::toResponse));
    }

    public Mono<PatientResponse> savePatient(PatientRequest body) {
        LocalDateTime now = LocalDateTime.now();
        return userRepository.findById(body.getUserId())
                .filter(u -> u.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado.")))
                .flatMap(user -> patientRepository.findByUserIdAndDeletedAtIsNull(user.getId())
                        .hasElement()
                        .flatMap(exists -> exists
                                ? Mono.<User>error(new ConflictException("El usuario ya tiene ficha de paciente."))
                                : Mono.just(user)))
                .flatMap(user -> {
                    user.setFullName(body.getFullName().trim());
                    user.setEmail(body.getEmail().trim().toLowerCase());
                    user.setPhone(body.getPhone().trim());
                    user.setUpdatedAt(now);
                    return userRepository.save(user);
                })
                .flatMap(user -> {
                    Patient patient = Patient.builder()
                            .userId(user.getId())
                            .documentNumber(body.getDocumentNumber().trim())
                            .birthDate(body.getBirthDate())
                            .active(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return patientRepository.save(patient);
                })
                .flatMap(p -> patientRepository.findDetailById(p.getId()).map(patientMapper::toResponse));
    }

    public Mono<PatientResponse> updatePatient(PatientRequest body) {
        if (body.getId() == null) {
            return Mono.error(new BadRequestException("Id del paciente es nulo."));
        }
        LocalDateTime now = LocalDateTime.now();
        return patientRepository.findById(body.getId())
                .filter(p -> p.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Paciente no encontrado.")))
                .flatMap(patient -> userRepository.findById(patient.getUserId())
                        .filter(u -> u.getDeletedAt() == null)
                        .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado.")))
                        .flatMap(user -> {
                            user.setFullName(body.getFullName().trim());
                            user.setEmail(body.getEmail().trim().toLowerCase());
                            user.setPhone(body.getPhone().trim());
                            user.setActive(body.isActive());
                            user.setUpdatedAt(now);
                            return userRepository.save(user).thenReturn(patient);
                        }))
                .flatMap(patient -> {
                    patient.setDocumentNumber(body.getDocumentNumber().trim());
                    patient.setBirthDate(body.getBirthDate());
                    patient.setActive(body.isActive());
                    patient.setUpdatedAt(now);
                    return patientRepository.save(patient);
                })
                .flatMap(p -> patientRepository.findDetailById(p.getId()).map(patientMapper::toResponse));
    }

    public Mono<Void> deletePatientById(UUID id) {
        LocalDateTime now = LocalDateTime.now();
        return patientRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Paciente no encontrado.")))
                .flatMap(patient -> userRepository.findById(patient.getUserId())
                        .flatMap(user -> {
                            user.setDeletedAt(now);
                            user.setUpdatedAt(now);
                            patient.setDeletedAt(now);
                            patient.setUpdatedAt(now);
                            return userRepository.save(user).then(patientRepository.save(patient));
                        }))
                .then();
    }
}
