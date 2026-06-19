package com.app.hubble.service;

import com.app.hubble.config.HubbleAppProperties;
import com.app.hubble.config.JwtProperties;
import com.app.hubble.dto.auth.LoginRequest;
import com.app.hubble.dto.auth.RefreshRequest;
import com.app.hubble.entity.User;
import com.app.hubble.entity.UserSession;
import com.app.hubble.enumeration.UserRole;
import com.app.hubble.exception.UnauthorizedException;
import com.app.hubble.repository.PasswordResetRepository;
import com.app.hubble.repository.UserRepository;
import com.app.hubble.repository.UserSessionRepository;
import com.app.hubble.security.JwtService;
import com.app.hubble.util.HashUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSessionRepository userSessionRepository;
    @Mock
    private PasswordResetRepository passwordResetRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private HubbleAppProperties appProperties;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    private ServerWebExchange exchange;
    private User activeUser;

    @BeforeEach
    void setUp() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/auth/login")
                .header(HttpHeaders.USER_AGENT, "JUnit-Test")
                .build();
        exchange = MockServerWebExchange.from(request);
        activeUser = User.builder()
                .id(UUID.randomUUID())
                .email("paciente@test.com")
                .passwordHash("encoded-hash")
                .fullName("Paciente Test")
                .phone("3001234567")
                .role(UserRole.PATIENT)
                .active(true)
                .documentNumber("1234567890")
                .birthDate(LocalDate.of(1990, 1, 15))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void loginConCredencialesValidasEmiteTokens() {
        LoginRequest request = LoginRequest.builder()
                .email("paciente@test.com")
                .password("Secret123")
                .build();
        when(userRepository.findActiveByEmail("paciente@test.com")).thenReturn(Mono.just(activeUser));
        when(passwordEncoder.matches("Secret123", "encoded-hash")).thenReturn(true);
        when(jwtService.createAccessToken(eq(activeUser.getId()), eq(UserRole.PATIENT), any()))
                .thenReturn("access-token");
        when(jwtProperties.accessMinutes()).thenReturn(15);
        when(jwtProperties.refreshDays()).thenReturn(30);
        when(userSessionRepository.save(any(UserSession.class))).thenAnswer(invocation -> {
            UserSession session = invocation.getArgument(0);
            session.setId(UUID.randomUUID());
            return Mono.just(session);
        });
        when(userService.toResponse(activeUser)).thenReturn(
                com.app.hubble.dto.response.UserResponse.builder()
                        .id(activeUser.getId())
                        .email(activeUser.getEmail())
                        .fullName(activeUser.getFullName())
                        .phone(activeUser.getPhone())
                        .role(activeUser.getRole())
                        .active(activeUser.isActive())
                        .documentNumber(activeUser.getDocumentNumber())
                        .birthDate(activeUser.getBirthDate())
                        .build()
        );
        StepVerifier.create(authService.login(request, exchange))
                .assertNext(response -> {
                    org.assertj.core.api.Assertions.assertThat(response.getAccessToken()).isEqualTo("access-token");
                    org.assertj.core.api.Assertions.assertThat(response.getRefreshToken()).isNotBlank();
                    org.assertj.core.api.Assertions.assertThat(response.getTokenType()).isEqualTo("Bearer");
                    org.assertj.core.api.Assertions.assertThat(response.getExpiresIn()).isEqualTo(900L);
                    org.assertj.core.api.Assertions.assertThat(response.getUser().getEmail()).isEqualTo("paciente@test.com");
                })
                .verifyComplete();
        verify(userSessionRepository).save(any(UserSession.class));
    }

    @Test
    void loginConContrasenaIncorrectaFalla() {
        LoginRequest request = LoginRequest.builder()
                .email("paciente@test.com")
                .password("Incorrecta")
                .build();
        when(userRepository.findActiveByEmail("paciente@test.com")).thenReturn(Mono.just(activeUser));
        when(passwordEncoder.matches("Incorrecta", "encoded-hash")).thenReturn(false);
        StepVerifier.create(authService.login(request, exchange))
                .expectErrorMatches(error -> error instanceof UnauthorizedException
                        && "Credenciales incorrectas".equals(error.getMessage()))
                .verify();
        verify(userSessionRepository, never()).save(any(UserSession.class));
    }

    @Test
    void loginConCorreoInexistenteFalla() {
        LoginRequest request = LoginRequest.builder()
                .email("desconocido@test.com")
                .password("Secret123")
                .build();
        when(userRepository.findActiveByEmail("desconocido@test.com")).thenReturn(Mono.empty());
        StepVerifier.create(authService.login(request, exchange))
                .expectError(UnauthorizedException.class)
                .verify();
    }

    @Test
    void refreshConTokenInvalidoFalla() {
        RefreshRequest request = RefreshRequest.builder()
                .refreshToken("token-invalido")
                .build();
        String hash = HashUtils.sha256Hex("token-invalido");
        when(userSessionRepository.findActiveByRefreshTokenHash(hash)).thenReturn(Mono.empty());
        StepVerifier.create(authService.refresh(request, exchange))
                .expectErrorMatches(error -> error instanceof UnauthorizedException
                        && "Sesión inválida".equals(error.getMessage()))
                .verify();
    }
}
