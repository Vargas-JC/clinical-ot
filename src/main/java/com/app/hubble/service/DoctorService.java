package com.app.hubble.service;

import com.app.hubble.dto.request.DoctorRequest;
import com.app.hubble.dto.response.DoctorResponse;
import com.app.hubble.entity.Doctor;
import com.app.hubble.entity.User;
import com.app.hubble.exception.BadRequestException;
import com.app.hubble.exception.ConflictException;
import com.app.hubble.exception.NotFoundException;
import com.app.hubble.mapper.DoctorMapper;
import com.app.hubble.repository.DoctorRepository;
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
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DoctorMapper doctorMapper;

    public Mono<PageResponse<DoctorResponse>> findAllDoctors(int page, int size) {
        int offset = page * size;
        Flux<DoctorResponse> data = doctorRepository.findAllPagedDetail(size, offset)
                .map(doctorMapper::toResponse);
        Mono<Long> total = doctorRepository.countByDeletedAtIsNull();
        return PageUtils.buildPage(data, total, page, size);
    }

    public Mono<DoctorResponse> findDoctorById(UUID id) {
        return doctorRepository.findDetailById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Doctor no encontrado.")))
                .map(doctorMapper::toResponse);
    }

    public Mono<DoctorResponse> findDoctorProfileForCurrentUser(UUID userId) {
        return doctorRepository.findByUserIdAndDeletedAtIsNull(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("No tiene perfil de doctor.")))
                .flatMap(d -> doctorRepository.findDetailById(d.getId()).map(doctorMapper::toResponse));
    }

    public Mono<DoctorResponse> saveDoctor(DoctorRequest body) {
        LocalDateTime now = LocalDateTime.now();
        return userRepository.findById(body.getUserId())
                .filter(u -> u.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado.")))
                .flatMap(user -> doctorRepository.existsByUserIdAndDeletedAtIsNull(user.getId())
                        .flatMap(exists -> exists
                                ? Mono.<User>error(new ConflictException("El usuario ya tiene perfil de doctor."))
                                : Mono.just(user)))
                .flatMap(user -> {
                    user.setFullName(body.getFullName().trim());
                    user.setEmail(body.getEmail().trim().toLowerCase());
                    user.setPhone(body.getPhone().trim());
                    user.setUpdatedAt(now);
                    return userRepository.save(user);
                })
                .flatMap(user -> {
                    Doctor doctor = Doctor.builder()
                            .userId(user.getId())
                            .specialty(body.getSpecialty())
                            .active(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return doctorRepository.save(doctor);
                })
                .flatMap(d -> doctorRepository.findDetailById(d.getId()).map(doctorMapper::toResponse));
    }

    public Mono<DoctorResponse> updateDoctor(DoctorRequest body) {
        if (body.getId() == null) {
            return Mono.error(new BadRequestException("Id del doctor es nulo."));
        }
        LocalDateTime now = LocalDateTime.now();
        return doctorRepository.findById(body.getId())
                .filter(d -> d.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Doctor no encontrado.")))
                .flatMap(doctor -> userRepository.findById(doctor.getUserId())
                        .filter(u -> u.getDeletedAt() == null)
                        .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado.")))
                        .flatMap(user -> {
                            user.setFullName(body.getFullName().trim());
                            user.setEmail(body.getEmail().trim().toLowerCase());
                            user.setPhone(body.getPhone().trim());
                            user.setActive(body.isActive());
                            user.setUpdatedAt(now);
                            return userRepository.save(user).thenReturn(doctor);
                        }))
                .flatMap(doctor -> {
                    doctor.setSpecialty(body.getSpecialty());
                    doctor.setActive(body.isActive());
                    doctor.setUpdatedAt(now);
                    return doctorRepository.save(doctor);
                })
                .flatMap(d -> doctorRepository.findDetailById(d.getId()).map(doctorMapper::toResponse));
    }

    public Mono<Void> deleteDoctor(UUID id) {
        LocalDateTime now = LocalDateTime.now();
        return doctorRepository.findById(id)
                .filter(d -> d.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Doctor no encontrado.")))
                .flatMap(doctor -> {
                    doctor.setDeletedAt(now);
                    doctor.setUpdatedAt(now);
                    return doctorRepository.save(doctor);
                })
                .then();
    }
}
