package com.app.hubble.controller;

import com.app.hubble.dto.auth.AuthResponse;
import com.app.hubble.dto.auth.LoginRequest;
import com.app.hubble.dto.response.UserResponse;
import com.app.hubble.enumeration.UserRole;
import com.app.hubble.exception.HandleGlobalException;
import com.app.hubble.exception.UnauthorizedException;
import com.app.hubble.service.AuthService;
import com.app.hubble.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerWebTest {

    private WebTestClient webTestClient;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        UserService userService = mock(UserService.class);
        AuthController controller = new AuthController(authService, userService);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        webTestClient = WebTestClient.bindToController(controller)
                .controllerAdvice(new HandleGlobalException())
                .validator(validator)
                .build();
    }

    @Test
    void loginConCuerpoInvalidoRetorna400() {
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "email": "",
                          "password": ""
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo(400)
                .jsonPath("$.message").exists();
    }

    @Test
    void loginConCredencialesInvalidasRetorna401() {
        when(authService.login(any(LoginRequest.class), any(ServerWebExchange.class)))
                .thenReturn(Mono.error(new UnauthorizedException("Credenciales incorrectas")));
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "email": "paciente@test.com",
                          "password": "Secret123"
                        }
                        """)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo(401)
                .jsonPath("$.message").isEqualTo("Credenciales incorrectas");
    }

    @Test
    void loginExitosoRetorna200ConTokens() {
        UUID userId = UUID.randomUUID();
        AuthResponse response = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(UserResponse.builder()
                        .id(userId)
                        .email("paciente@test.com")
                        .fullName("Paciente Test")
                        .phone("3001234567")
                        .role(UserRole.PATIENT)
                        .active(true)
                        .documentNumber("1234567890")
                        .build())
                .build();
        when(authService.login(any(LoginRequest.class), any(ServerWebExchange.class)))
                .thenReturn(Mono.just(response));
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "email": "paciente@test.com",
                          "password": "Secret123"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("access-token")
                .jsonPath("$.refreshToken").isEqualTo("refresh-token")
                .jsonPath("$.tokenType").isEqualTo("Bearer")
                .jsonPath("$.user.email").isEqualTo("paciente@test.com");
    }
}
