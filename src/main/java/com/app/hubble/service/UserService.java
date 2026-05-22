package com.app.hubble.service;

import com.app.hubble.dto.request.PatientProfileUpdateRequest;
import com.app.hubble.dto.request.PatientRegisterRequest;
import com.app.hubble.dto.request.UserRequest;
import com.app.hubble.dto.response.UserResponse;
import com.app.hubble.entity.User;
import com.app.hubble.enumeration.UserRole;
import com.app.hubble.exception.BadRequestException;
import com.app.hubble.exception.ConflictException;
import com.app.hubble.exception.ForbiddenException;
import com.app.hubble.exception.NotFoundException;
import com.app.hubble.repository.UserRepository;
import org.springframework.http.codec.multipart.FilePart;
import com.app.hubble.util.PageResponse;
import com.app.hubble.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;

    public Mono<PageResponse<UserResponse>> findAllUsers(int page, int size) {
        int offset = page * size;
        Flux<UserResponse> data = userRepository.findAllPaged(size, offset)
                .map(this::toResponse);
        Mono<Long> total = userRepository.countByDeletedAtIsNull();
        return PageUtils.buildPage(data, total, page, size);
    }

    public Mono<UserResponse> findUserById(UUID id) {
        return userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado.")))
                .map(this::toResponse);
    }

    public Mono<Void> assertRegistrationAvailable(String email, String documentNumber) {
        String normalizedEmail = email.trim().toLowerCase();
        String normalizedDocument = documentNumber.trim();
        return userRepository.findActiveByEmail(normalizedEmail)
                .flatMap(u -> Mono.<Void>error(new ConflictException("Correo ya registrado.")))
                .switchIfEmpty(userRepository.findActiveByDocumentNumber(normalizedDocument)
                        .flatMap(u -> Mono.<Void>error(new ConflictException("Número de documento ya registrado.")))
                        .then());
    }

    public Mono<UserResponse> registerPatient(PatientRegisterRequest body) {
        LocalDateTime now = LocalDateTime.now();
        return assertRegistrationAvailable(body.getEmail(), body.getDocumentNumber())
                .then(Mono.defer(() -> {
                    User user = User.builder()
                            .email(body.getEmail().trim().toLowerCase())
                            .passwordHash(passwordEncoder.encode(body.getPassword()))
                            .fullName(body.getFullName().trim())
                            .phone(body.getPhone().trim())
                            .role(UserRole.PATIENT)
                            .active(true)
                            .documentNumber(body.getDocumentNumber().trim())
                            .birthDate(body.getBirthDate())
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return userRepository.save(user)
                            .flatMap(savedUser -> notificationService.onUserRegistered(savedUser)
                                    .onErrorResume(e -> Mono.empty())
                                    .thenReturn(savedUser));
                }))
                .map(this::toResponse);
    }

    public Mono<UserResponse> updatePatientProfile(UUID userId, PatientProfileUpdateRequest body, FilePart avatarFile) {
        LocalDateTime now = LocalDateTime.now();
        return userRepository.findById(userId)
                .filter(user -> user.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado.")))
                .flatMap(user -> {
                    if (user.getRole() != UserRole.PATIENT) {
                        return Mono.error(new ForbiddenException("Solo pacientes pueden actualizar este perfil."));
                    }
                    return assertDocumentAvailable(body.getDocumentNumber().trim(), user.getId())
                            .then(resolveAvatarUpdate(body, avatarFile, user))
                            .flatMap(avatarState -> applyPatientProfile(user, body, avatarState, now));
                });
    }

    private Mono<AvatarUpdateState> resolveAvatarUpdate(
            PatientProfileUpdateRequest body,
            FilePart avatarFile,
            User user
    ) {
        if (avatarFile != null) {
            return fileStorageService.storeAvatar(avatarFile)
                    .map(response -> new AvatarUpdateState(response.getId(), true));
        }
        if (body.isRemoveAvatar()) {
            return Mono.just(new AvatarUpdateState(null, true));
        }
        if (body.getAvatarFileId() != null) {
            return fileStorageService.assertFileExists(body.getAvatarFileId())
                    .thenReturn(new AvatarUpdateState(body.getAvatarFileId(), true));
        }
        return Mono.just(new AvatarUpdateState(user.getAvatarFileId(), false));
    }

    private Mono<UserResponse> applyPatientProfile(
            User user,
            PatientProfileUpdateRequest body,
            AvatarUpdateState avatarState,
            LocalDateTime now
    ) {
        String fullName = body.getFullName().trim();
        String phone = body.getPhone().trim();
        String documentNumber = body.getDocumentNumber().trim();
        Mono<Integer> updateMono;
        if (avatarState.updateAvatar()) {
            updateMono = userRepository.updatePatientProfileWithAvatarById(
                    user.getId(),
                    fullName,
                    phone,
                    documentNumber,
                    body.getBirthDate(),
                    avatarState.avatarFileId(),
                    now
            );
        } else {
            updateMono = userRepository.updatePatientProfileById(
                    user.getId(),
                    fullName,
                    phone,
                    documentNumber,
                    body.getBirthDate(),
                    now
            );
        }
        return updateMono
                .flatMap(rows -> {
                    if (rows == 0) {
                        return Mono.error(new NotFoundException("Usuario no encontrado."));
                    }
                    return userRepository.findById(user.getId());
                })
                .map(this::toResponse);
    }

    private record AvatarUpdateState(UUID avatarFileId, boolean updateAvatar) {
    }

    private Mono<Void> assertDocumentAvailable(String documentNumber, UUID userId) {
        return userRepository.findActiveByDocumentNumber(documentNumber)
                .flatMap(existing -> {
                    if (!existing.getId().equals(userId)) {
                        return Mono.<Void>error(new ConflictException("Número de documento ya registrado."));
                    }
                    return Mono.empty();
                })
                .switchIfEmpty(Mono.empty())
                .then();
    }

    public Mono<UserResponse> updateUser(UserRequest body) {
        if (body.getId() == null) {
            return Mono.error(new BadRequestException("El id de usuario es nulo."));
        }
        LocalDateTime now = LocalDateTime.now();
        return userRepository.findById(body.getId())
                .filter(u -> u.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado.")))
                .flatMap(user -> {
                    user.setEmail(body.getEmail().trim().toLowerCase());
                    user.setFullName(body.getFullName().trim());
                    user.setPhone(body.getPhone().trim());
                    user.setRole(body.getRole());
                    user.setActive(body.isActive());
                    user.setDocumentNumber(body.getDocumentNumber());
                    user.setBirthDate(body.getBirthDate());
                    user.setSpecialty(body.getSpecialty());
                    user.setUpdatedAt(now);
                    return userRepository.save(user);
                })
                .map(this::toResponse);
    }

    public Mono<Void> deleteUserById(UUID id) {
        LocalDateTime now = LocalDateTime.now();
        return userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .switchIfEmpty(Mono.error(new NotFoundException("Usuario no encontrado.")))
                .flatMap(user -> {
                    user.setDeletedAt(now);
                    user.setUpdatedAt(now);
                    return userRepository.save(user);
                })
                .then();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .active(user.isActive())
                .documentNumber(user.getDocumentNumber())
                .birthDate(user.getBirthDate())
                .specialty(user.getSpecialty())
                .avatarFileId(user.getAvatarFileId())
                .avatarUrl(fileStorageService.buildPublicUrl(user.getAvatarFileId()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
