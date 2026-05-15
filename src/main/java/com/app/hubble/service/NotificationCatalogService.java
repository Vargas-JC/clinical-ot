package com.app.hubble.service;

import com.app.hubble.dto.request.NotificationRequest;
import com.app.hubble.dto.response.NotificationResponse;
import com.app.hubble.entity.Notification;
import com.app.hubble.entity.User;
import com.app.hubble.enumeration.UserRole;
import com.app.hubble.exception.BadRequestException;
import com.app.hubble.exception.NotFoundException;
import com.app.hubble.exception.UnauthorizedException;
import com.app.hubble.repository.AppointmentRepository;
import com.app.hubble.repository.NotificationRepository;
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
public class NotificationCatalogService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;

    public Mono<PageResponse<NotificationResponse>> findAllNotifications(int page, int size) {
        int offset = page * size;
        Flux<NotificationResponse> data = notificationRepository.findAllActivePaged(size, offset).map(this::toResponse);
        Mono<Long> total = notificationRepository.countByDeletedAtIsNull();
        return PageUtils.buildPage(data, total, page, size);
    }

    public Mono<PageResponse<NotificationResponse>> findNotificationsForCurrentUser(UUID userId, int page, int size) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .filter(u -> u.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new UnauthorizedException("Usuario no encontrado.")))
                .flatMap(user -> {
                    if (user.getRole() == UserRole.ADMIN) {
                        return findAllNotifications(page, size);
                    }
                    int offset = page * size;
                    Flux<NotificationResponse> data = notificationRepository.findPagedByUserId(userId, size, offset)
                            .map(this::toResponse);
                    Mono<Long> total = notificationRepository.countByUserId(userId);
                    return PageUtils.buildPage(data, total, page, size);
                });
    }

    public Mono<NotificationResponse> findNotificationById(UUID id) {
        return notificationRepository.findById(id)
                .filter(n -> n.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Notificación no encontrada.")))
                .map(this::toResponse);
    }

    public Mono<NotificationResponse> saveNotification(NotificationRequest body) {
        LocalDateTime now = LocalDateTime.now();
        return userRepository.findById(body.getUserId())
                .filter(u -> u.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado.")))
                .flatMap(u -> requireAppointmentIfPresent(body.getAppointmentId())
                        .then(requirePaymentIfPresent(body.getPaymentId()))
                        .thenReturn(u))
                .flatMap(u -> {
                    Notification row = Notification.builder()
                            .userId(body.getUserId())
                            .type(body.getType())
                            .title(body.getTitle().trim())
                            .body(body.getBody().trim())
                            .appointmentId(body.getAppointmentId())
                            .paymentId(body.getPaymentId())
                            .active(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return notificationRepository.save(row);
                })
                .map(this::toResponse);
    }

    public Mono<NotificationResponse> updateNotification(NotificationRequest body) {
        if (body.getId() == null) {
            return Mono.error(new BadRequestException("Id de notificación es nulo."));
        }
        LocalDateTime now = LocalDateTime.now();
        return notificationRepository.findById(body.getId())
                .filter(n -> n.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Notificación no encontrada.")))
                .flatMap(row -> userRepository.findById(body.getUserId())
                        .filter(u -> u.getDeletedAt() == null)
                        .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado.")))
                        .flatMap(u -> requireAppointmentIfPresent(body.getAppointmentId())
                                .then(requirePaymentIfPresent(body.getPaymentId()))
                                .thenReturn(u))
                        .flatMap(u -> {
                            row.setUserId(body.getUserId());
                            row.setType(body.getType());
                            row.setTitle(body.getTitle().trim());
                            row.setBody(body.getBody().trim());
                            row.setAppointmentId(body.getAppointmentId());
                            row.setPaymentId(body.getPaymentId());
                            row.setActive(body.isActive());
                            row.setUpdatedAt(now);
                            return notificationRepository.save(row);
                        }))
                .map(this::toResponse);
    }

    public Mono<Void> deleteNotification(UUID id) {
        LocalDateTime now = LocalDateTime.now();
        return notificationRepository.findById(id)
                .filter(n -> n.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Notificación no encontrada.")))
                .flatMap(row -> {
                    row.setDeletedAt(now);
                    row.setUpdatedAt(now);
                    row.setActive(false);
                    return notificationRepository.save(row);
                })
                .then();
    }

    private Mono<Void> requireAppointmentIfPresent(UUID appointmentId) {
        if (appointmentId == null) {
            return Mono.empty();
        }
        return appointmentRepository.findById(appointmentId)
                .filter(a -> a.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Cita no encontrada.")))
                .then();
    }

    private Mono<Void> requirePaymentIfPresent(UUID paymentId) {
        if (paymentId == null) {
            return Mono.empty();
        }
        return paymentRepository.findById(paymentId)
                .filter(p -> p.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Pago no encontrado.")))
                .then();
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .type(n.getType())
                .title(n.getTitle())
                .body(n.getBody())
                .appointmentId(n.getAppointmentId())
                .paymentId(n.getPaymentId())
                .emailSentAt(n.getEmailSentAt())
                .appSentAt(n.getAppSentAt())
                .active(n.isActive())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}
