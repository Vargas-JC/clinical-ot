package com.app.hubble.service;

import com.app.hubble.dto.response.UserResponse;
import com.app.hubble.entity.User;
import com.app.hubble.enumeration.UserRole;
import com.app.hubble.exception.ConflictException;
import com.app.hubble.exception.NotFoundException;
import com.app.hubble.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private NotificationService notificationService;
    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private UserService userService;

    @Test
    void assertRegistrationAvailableFallaCuandoCorreoExiste() {
        User existing = buildUser("existente@test.com", "111");
        when(userRepository.findActiveByEmail("existente@test.com")).thenReturn(Mono.just(existing));
        when(userRepository.findActiveByDocumentNumber("999")).thenReturn(Mono.empty());
        StepVerifier.create(userService.assertRegistrationAvailable("existente@test.com", "999"))
                .expectErrorMatches(error -> error instanceof ConflictException
                        && "Correo ya registrado.".equals(error.getMessage()))
                .verify();
    }

    @Test
    void assertRegistrationAvailableFallaCuandoDocumentoExiste() {
        User existing = buildUser("otro@test.com", "555");
        when(userRepository.findActiveByEmail("nuevo@test.com")).thenReturn(Mono.empty());
        when(userRepository.findActiveByDocumentNumber("555")).thenReturn(Mono.just(existing));
        StepVerifier.create(userService.assertRegistrationAvailable("nuevo@test.com", "555"))
                .expectErrorMatches(error -> error instanceof ConflictException
                        && "Número de documento ya registrado.".equals(error.getMessage()))
                .verify();
    }

    @Test
    void assertRegistrationAvailableCompletaCuandoDatosEstanLibres() {
        when(userRepository.findActiveByEmail("libre@test.com")).thenReturn(Mono.empty());
        when(userRepository.findActiveByDocumentNumber("777")).thenReturn(Mono.empty());
        StepVerifier.create(userService.assertRegistrationAvailable("libre@test.com", "777"))
                .verifyComplete();
    }

    @Test
    void findUserByIdFallaCuandoNoExiste() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Mono.empty());
        StepVerifier.create(userService.findUserById(userId))
                .expectErrorMatches(error -> error instanceof NotFoundException
                        && "Usuario no encontrado.".equals(error.getMessage()))
                .verify();
    }

    @Test
    void findUserByIdFallaCuandoUsuarioEstaEliminado() {
        UUID userId = UUID.randomUUID();
        User deletedUser = buildUser("eliminado@test.com", "888");
        deletedUser.setId(userId);
        deletedUser.setDeletedAt(LocalDateTime.now());
        when(userRepository.findById(userId)).thenReturn(Mono.just(deletedUser));
        StepVerifier.create(userService.findUserById(userId))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void findUserByIdRetornaUsuarioActivo() {
        UUID userId = UUID.randomUUID();
        User user = buildUser("activo@test.com", "444");
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(fileStorageService.buildPublicUrl(user.getAvatarFileId())).thenReturn("");
        StepVerifier.create(userService.findUserById(userId))
                .assertNext(response -> {
                    org.assertj.core.api.Assertions.assertThat(response.getId()).isEqualTo(userId);
                    org.assertj.core.api.Assertions.assertThat(response.getEmail()).isEqualTo("activo@test.com");
                })
                .verifyComplete();
    }

    private User buildUser(String email, String documentNumber) {
        return User.builder()
                .email(email)
                .passwordHash("hash")
                .fullName("Usuario Test")
                .phone("3000000000")
                .role(UserRole.PATIENT)
                .active(true)
                .documentNumber(documentNumber)
                .birthDate(LocalDate.of(1995, 5, 20))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
