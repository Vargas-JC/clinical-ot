package com.app.hubble.service;

import com.app.hubble.dto.request.AppointmentRequest;
import com.app.hubble.dto.response.AppointmentResponse;
import com.app.hubble.entity.Appointment;
import com.app.hubble.entity.User;
import com.app.hubble.enumeration.AppointmentStatus;
import com.app.hubble.enumeration.UserRole;
import com.app.hubble.exception.BadRequestException;
import com.app.hubble.exception.ForbiddenException;
import com.app.hubble.exception.NotFoundException;
import com.app.hubble.exception.UnauthorizedException;
import com.app.hubble.mapper.AppointmentMapper;
import com.app.hubble.entity.Doctor;
import com.app.hubble.entity.Patient;
import com.app.hubble.repository.AppointmentRepository;
import com.app.hubble.repository.DoctorRepository;
import com.app.hubble.repository.PatientRepository;
import com.app.hubble.repository.UserRepository;
import com.app.hubble.repository.view.AppointmentView;
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
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public Mono<PageResponse<AppointmentResponse>> findAllAppointments(int page, int size) {
        int offset = page * size;
        Flux<AppointmentResponse> data = appointmentRepository.findAllPaged(size, offset)
                .map(this::toResponse);
        Mono<Long> total = appointmentRepository.countActive();
        return PageUtils.buildPage(data, total, page, size);
    }

    public Mono<PageResponse<AppointmentResponse>> findAppointmentsForCurrentUser(UUID userId, int page, int size) {
        int offset = page * size;
        return userRepository.findById(userId)
                .filter(User::isActive)
                .filter(u -> u.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new UnauthorizedException("Usuario no encontrado.")))
                .flatMap(user -> {
                    if (user.getRole() == UserRole.ADMIN) {
                        return findAllAppointments(page, size);
                    }
                    Mono<Patient> patientMono = patientRepository.findByUserIdAndDeletedAtIsNull(userId);
                    Mono<Doctor> doctorMono = doctorRepository.findByUserIdAndDeletedAtIsNull(userId);
                    return patientMono
                            .flatMap(patient -> pageAppointmentsForPatient(patient.getId(), page, size, offset))
                            .switchIfEmpty(doctorMono
                                    .flatMap(doctor -> pageAppointmentsForDoctor(doctor.getId(), page, size, offset))
                                    .switchIfEmpty(Mono.error(new ForbiddenException(
                                            "No hay perfil de paciente ni de doctor asociado a la sesión."))));
                });
    }

    private Mono<PageResponse<AppointmentResponse>> pageAppointmentsForPatient(
            UUID patientId,
            int page,
            int size,
            int offset) {
        Flux<AppointmentResponse> data = appointmentRepository.findPagedByPatientId(patientId, size, offset)
                .map(this::toResponse);
        Mono<Long> total = appointmentRepository.countActiveByPatientId(patientId);
        return PageUtils.buildPage(data, total, page, size);
    }

    private Mono<PageResponse<AppointmentResponse>> pageAppointmentsForDoctor(
            UUID doctorId,
            int page,
            int size,
            int offset) {
        Flux<AppointmentResponse> data = appointmentRepository.findPagedByDoctorId(doctorId, size, offset)
                .map(this::toResponse);
        Mono<Long> total = appointmentRepository.countActiveByDoctorId(doctorId);
        return PageUtils.buildPage(data, total, page, size);
    }

    public Mono<AppointmentResponse> findAppointmentById(UUID id) {
        return appointmentRepository.findAppointmentById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Cita no encontrada.")))
                .map(this::toResponse);
    }

    public Mono<AppointmentResponse> saveAppointment(AppointmentRequest body) {
        LocalDateTime now = LocalDateTime.now();
        return Mono.zip(
                        patientRepository.findById(body.getPatientId())
                                .filter(p -> p.getDeletedAt() == null)
                                .switchIfEmpty(Mono.error(new NotFoundException("Paciente no encontrado."))),
                        doctorRepository.findById(body.getDoctorId())
                                .filter(d -> d.getDeletedAt() == null)
                                .switchIfEmpty(Mono.error(new NotFoundException("Doctor no encontrado.")))
                )
                .flatMap(tuple -> {
                    Appointment appointment = appointmentMapper.toEntity(body);
                    appointment.setPatientId(tuple.getT1().getId());
                    appointment.setDoctorId(tuple.getT2().getId());
                    appointment.setActive(true);
                    appointment.setCreatedAt(now);
                    appointment.setUpdatedAt(now);
                    if (appointment.getStatus() == null) {
                        appointment.setStatus(AppointmentStatus.SCHEDULED);
                    }
                    return appointmentRepository.save(appointment);
                })
                .flatMap(saved -> patientRepository.findById(saved.getPatientId())
                        .flatMap(p -> userRepository.findById(p.getUserId())
                                .flatMap(u -> notificationService.onAppointmentReserved(
                                                saved.getId(),
                                                p.getUserId(),
                                                u.getFullName(),
                                                saved.getAppointmentDate())
                                        .onErrorResume(e -> Mono.empty())
                                        .thenReturn(saved))))
                .flatMap(saved -> appointmentRepository.findAppointmentById(saved.getId()).map(this::toResponse));
    }

    public Mono<AppointmentResponse> updateAppointment(AppointmentRequest body) {
        if (body.getId() == null) {
            return Mono.error(new BadRequestException("Id de la cita es nulo."));
        }
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findById(body.getId())
                .filter(a -> a.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Cita no encontrada.")))
                .flatMap(appointment ->
                        Mono.zip(
                                        patientRepository.findById(body.getPatientId())
                                                .filter(p -> p.getDeletedAt() == null)
                                                .switchIfEmpty(Mono.error(new NotFoundException("Paciente no encontrado."))),
                                        doctorRepository.findById(body.getDoctorId())
                                                .filter(d -> d.getDeletedAt() == null)
                                                .switchIfEmpty(Mono.error(new NotFoundException("Doctor no encontrado.")))
                                )
                                .map(tuple -> {
                                    appointment.setPatientId(tuple.getT1().getId());
                                    appointment.setDoctorId(tuple.getT2().getId());
                                    appointment.setStatus(body.getStatus());
                                    appointment.setActive(body.isActive());
                                    appointment.setReason(body.getReason());
                                    appointment.setUpdatedAt(now);
                                    appointment.setAppointmentDate(body.getAppointmentDate());
                                    return appointment;
                                })
                )
                .flatMap(appointmentRepository::save)
                .flatMap(saved -> appointmentRepository.findAppointmentById(saved.getId()).map(this::toResponse));
    }

    public Mono<Void> deleteAppointment(UUID id) {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findById(id)
                .filter(a -> a.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Cita no encontrada.")))
                .flatMap(a -> {
                    a.setDeletedAt(now);
                    a.setUpdatedAt(now);
                    return appointmentRepository.save(a);
                })
                .then();
    }

    private AppointmentResponse toResponse(AppointmentView view) {
        return AppointmentResponse.builder()
                .id(view.getId())
                .patientId(view.getPatientId())
                .patientName(view.getPatientName())
                .doctorId(view.getDoctorId())
                .doctorName(view.getDoctorName())
                .reason(view.getReason())
                .appointmentDate(view.getAppointmentDate())
                .status(view.getStatus())
                .active(Boolean.TRUE.equals(view.getActive()))
                .createdAt(view.getCreatedAt())
                .updatedAt(view.getUpdatedAt())
                .build();
    }
}
