package com.app.hubble.service;

import com.app.hubble.dto.request.ExamRequest;
import com.app.hubble.dto.response.ExamResponse;
import com.app.hubble.entity.Exam;
import com.app.hubble.entity.User;
import com.app.hubble.enumeration.UserRole;
import com.app.hubble.exception.BadRequestException;
import com.app.hubble.exception.NotFoundException;
import com.app.hubble.exception.UnauthorizedException;
import com.app.hubble.repository.ExamRepository;
import com.app.hubble.repository.MedicalConsultationRepository;
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
public class ExamService {
    private final ExamRepository examRepository;
    private final MedicalConsultationRepository medicalConsultationRepository;
    private final UserRepository userRepository;

    public Mono<PageResponse<ExamResponse>> findAllExams(int page, int size) {
        int offset = page * size;
        Flux<ExamResponse> data = examRepository.findAllActivePaged(size, offset).map(this::toResponse);
        Mono<Long> total = examRepository.countByDeletedAtIsNull();
        return PageUtils.buildPage(data, total, page, size);
    }

    public Mono<PageResponse<ExamResponse>> findExamsForCurrentUser(UUID userId, int page, int size) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .filter(u -> u.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new UnauthorizedException("Usuario no encontrado.")))
                .flatMap(user -> {
                    if (user.getRole() == UserRole.ADMIN) {
                        return findAllExams(page, size);
                    }
                    return pageExamsForUser(userId, page, size);
                });
    }

    private Mono<PageResponse<ExamResponse>> pageExamsForUser(UUID userId, int page, int size) {
        int offset = page * size;
        Flux<ExamResponse> data = examRepository.findPagedByUserId(userId, size, offset).map(this::toResponse);
        Mono<Long> total = examRepository.countByUserId(userId);
        return PageUtils.buildPage(data, total, page, size);
    }

    public Mono<ExamResponse> findExamById(UUID id) {
        return examRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Examen no encontrado.")))
                .map(this::toResponse);
    }

    public Mono<ExamResponse> saveExam(ExamRequest body) {
        LocalDateTime now = LocalDateTime.now();
        return userRepository.findById(body.getUserId())
                .filter(u -> u.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado.")))
                .flatMap(u -> requireConsultationIfPresent(body.getConsultationId()).thenReturn(u))
                .flatMap(u -> {
                    Exam row = Exam.builder()
                            .userId(body.getUserId())
                            .consultationId(body.getConsultationId())
                            .type(body.getType())
                            .result(blankToNull(body.getResult()))
                            .active(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return examRepository.save(row);
                })
                .map(this::toResponse);
    }

    public Mono<ExamResponse> updateExam(ExamRequest body) {
        if (body.getId() == null) {
            return Mono.error(new BadRequestException("Id de examen es nulo."));
        }
        LocalDateTime now = LocalDateTime.now();
        return examRepository.findById(body.getId())
                .filter(e -> e.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Examen no encontrado.")))
                .flatMap(row -> userRepository.findById(body.getUserId())
                        .filter(u -> u.getDeletedAt() == null)
                        .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado.")))
                        .then(requireConsultationIfPresent(body.getConsultationId()))
                        .thenReturn(row)
                        .flatMap(r -> {
                            r.setUserId(body.getUserId());
                            r.setConsultationId(body.getConsultationId());
                            r.setType(body.getType());
                            r.setResult(blankToNull(body.getResult()));
                            r.setActive(body.isActive());
                            r.setUpdatedAt(now);
                            return examRepository.save(r);
                        }))
                .map(this::toResponse);
    }

    public Mono<Void> deleteExam(UUID id) {
        LocalDateTime now = LocalDateTime.now();
        return examRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Examen no encontrado.")))
                .flatMap(row -> {
                    row.setDeletedAt(now);
                    row.setUpdatedAt(now);
                    row.setActive(false);
                    return examRepository.save(row);
                })
                .then();
    }

    private Mono<Void> requireConsultationIfPresent(UUID consultationId) {
        if (consultationId == null) {
            return Mono.empty();
        }
        return medicalConsultationRepository.findById(consultationId)
                .filter(c -> c.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Consulta médica no encontrada.")))
                .then();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private ExamResponse toResponse(Exam e) {
        return ExamResponse.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .consultationId(e.getConsultationId())
                .type(e.getType())
                .result(e.getResult() == null ? "" : e.getResult())
                .active(e.isActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
