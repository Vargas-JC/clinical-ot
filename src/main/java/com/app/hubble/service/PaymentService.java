package com.app.hubble.service;

import com.app.hubble.dto.request.PaymentRequest;
import com.app.hubble.dto.response.PaymentResponse;
import com.app.hubble.entity.Payment;
import com.app.hubble.entity.User;
import com.app.hubble.enumeration.PaymentStatus;
import com.app.hubble.enumeration.UserRole;
import com.app.hubble.exception.BadRequestException;
import com.app.hubble.exception.ForbiddenException;
import com.app.hubble.exception.NotFoundException;
import com.app.hubble.exception.UnauthorizedException;
import com.app.hubble.mapper.PaymentMapper;
import com.app.hubble.repository.PatientRepository;
import com.app.hubble.repository.PaymentRepository;
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
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public Mono<PageResponse<PaymentResponse>> findAllPayments(int page, int size) {
        int offset = page * size;
        Flux<PaymentResponse> data = paymentRepository.findAllPaged(size, offset)
                .map(paymentMapper::toResponse);
        Mono<Long> total = paymentRepository.countActive();
        return PageUtils.buildPage(data, total, page, size);
    }

    public Mono<PageResponse<PaymentResponse>> findPaymentsForCurrentUser(UUID userId, int page, int size) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .filter(u -> u.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new UnauthorizedException("Usuario no encontrado.")))
                .flatMap(user -> {
                    if (user.getRole() == UserRole.ADMIN) {
                        return findAllPayments(page, size);
                    }
                    return patientRepository.findByUserIdAndDeletedAtIsNull(userId)
                            .switchIfEmpty(Mono.error(new ForbiddenException("Se requiere perfil de paciente.")))
                            .flatMap(patient -> pagePaymentsForPatient(patient.getId(), page, size));
                });
    }

    private Mono<PageResponse<PaymentResponse>> pagePaymentsForPatient(UUID patientId, int page, int size) {
        int offset = page * size;
        Flux<PaymentResponse> data = paymentRepository.findPagedByPatientId(patientId, size, offset)
                .map(paymentMapper::toResponse);
        Mono<Long> total = paymentRepository.countActiveByPatientId(patientId);
        return PageUtils.buildPage(data, total, page, size);
    }

    public Mono<PaymentResponse> findPaymentById(UUID id) {
        return paymentRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Pago no encontrado.")))
                .map(paymentMapper::toResponse);
    }

    public Mono<PaymentResponse> savePayment(PaymentRequest body) {
        LocalDateTime now = LocalDateTime.now();
        return patientRepository.findById(body.getPatientId())
                .filter(p -> p.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Paciente no encontrado.")))
                .flatMap(p -> {
                    Payment payment = paymentMapper.toEntity(body);
                    payment.setActive(true);
                    payment.setCreatedAt(now);
                    payment.setUpdatedAt(now);
                    return paymentRepository.save(payment);
                })
                .flatMap(saved -> maybeNotifyPaymentPaid(saved).thenReturn(saved))
                .map(paymentMapper::toResponse);
    }

    public Mono<PaymentResponse> updatePayment(PaymentRequest body) {
        if (body.getId() == null) {
            return Mono.error(new BadRequestException("Id del pago es nulo."));
        }
        LocalDateTime now = LocalDateTime.now();
        return paymentRepository.findById(body.getId())
                .filter(p -> p.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Pago no encontrado.")))
                .flatMap(existing -> patientRepository.findById(body.getPatientId())
                        .filter(p -> p.getDeletedAt() == null)
                        .switchIfEmpty(Mono.error(new NotFoundException("Paciente no encontrado.")))
                        .thenReturn(existing))
                .flatMap(existing -> {
                    PaymentStatus previous = existing.getStatus();
                    existing.setPatientId(body.getPatientId());
                    existing.setAppointmentId(body.getAppointmentId());
                    existing.setExamId(body.getExamId());
                    existing.setPrescriptionId(body.getPrescriptionId());
                    existing.setPaymentCardId(body.getPaymentCardId());
                    existing.setAmount(body.getAmount());
                    existing.setMethod(body.getMethod());
                    existing.setStatus(body.getStatus());
                    existing.setActive(body.isActive());
                    existing.setUpdatedAt(now);
                    return paymentRepository.save(existing)
                            .flatMap(saved -> {
                                boolean becamePaid = saved.getStatus() == PaymentStatus.PAID
                                        && previous != PaymentStatus.PAID;
                                if (becamePaid) {
                                    return maybeNotifyPaymentPaid(saved).thenReturn(saved);
                                }
                                return Mono.just(saved);
                            });
                })
                .map(paymentMapper::toResponse);
    }

    public Mono<Void> deletePayment(UUID id) {
        LocalDateTime now = LocalDateTime.now();
        return paymentRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Pago no encontrado.")))
                .flatMap(p -> {
                    p.setDeletedAt(now);
                    p.setUpdatedAt(now);
                    return paymentRepository.save(p);
                })
                .then();
    }

    private Mono<Void> maybeNotifyPaymentPaid(Payment saved) {
        if (saved.getStatus() != PaymentStatus.PAID) {
            return Mono.empty();
        }
        return patientRepository.findById(saved.getPatientId())
                .flatMap(p -> userRepository.findById(p.getUserId())
                        .flatMap(u -> notificationService.onPaymentCompleted(
                                saved.getId(),
                                p.getUserId(),
                                saved.getAmount().toPlainString()
                        ).onErrorResume(e -> Mono.empty())));
    }
}
