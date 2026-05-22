package com.app.hubble.controller;

import com.app.hubble.dto.auth.ForgotPasswordRequest;
import com.app.hubble.dto.auth.LoginRequest;
import com.app.hubble.dto.auth.RefreshRequest;
import com.app.hubble.dto.auth.RegisterRequest;
import com.app.hubble.dto.auth.ResetPasswordRequest;
import com.app.hubble.dto.auth.AuthResponse;
import com.app.hubble.dto.auth.RefreshTokenResponse;
import com.app.hubble.dto.request.PatientProfileUpdateRequest;
import com.app.hubble.dto.request.PatientRegisterRequest;
import com.app.hubble.dto.response.UserResponse;
import com.app.hubble.security.Autenticacion;
import com.app.hubble.service.AuthService;
import com.app.hubble.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Tag(name = "Auth", description = "Registro, sesión JWT (RSA), refresh, cierre de sesión y " +
                                  "recuperación de contraseña por código OTP (6 dígitos al correo)."
)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "Registro de usuario con rol y sesión JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens emitidos"),
            @ApiResponse(responseCode = "400", description = "Validación del cuerpo u otras reglas de negocio"),
            @ApiResponse(responseCode = "409", description = "Correo ya registrado")
    })
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest body,
            ServerWebExchange exchange
    ) {
        return authService.register(body, exchange).map(ResponseEntity::ok);
    }

    @Operation(summary = "Registro de paciente sin sesión (rol PATIENT)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Paciente registrado"),
            @ApiResponse(responseCode = "400", description = "Validación inválida"),
            @ApiResponse(responseCode = "409", description = "Correo ya registrado")
    })
    @PostMapping(value = "/register/patient", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UserResponse>> registerPatient(@Valid @RequestBody PatientRegisterRequest body) {
        return userService.registerPatient(body)
                .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(user));
    }

    @Operation(summary = "Inicio de sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens emitidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o usuario inactivo")
    })
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AuthResponse>> login(
            @Valid @RequestBody LoginRequest body,
            ServerWebExchange exchange
    ) {
        return authService.login(body, exchange).map(ResponseEntity::ok);
    }

    @Operation(summary = "Renovar access token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nuevos tokens"),
            @ApiResponse(responseCode = "401", description = "Refresh inválido, expirado o usuario inactivo")
    })
    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<RefreshTokenResponse>> refresh(
            @Valid @RequestBody RefreshRequest body,
            ServerWebExchange exchange
    ) {
        return authService.refresh(body, exchange).map(ResponseEntity::ok);
    }

    @Operation(summary = "Cerrar sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sesión revocada o refresh desconocido (sin contenido)"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido")
    })
    @PostMapping(value = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> logout(@Valid @RequestBody RefreshRequest body) {
        return authService.logout(body).thenReturn(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Solicitar recuperación de contraseña")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Proceso aceptado (no revela si el correo existe)"),
            @ApiResponse(responseCode = "400", description = "Validación del cuerpo")
    })
    @PostMapping(value = "/forgot-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest body) {
        return authService.forgotPassword(body).thenReturn(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Restablecer contraseña con código OTP")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contraseña actualizada"),
            @ApiResponse(responseCode = "400", description = "Código inválido o validación del cuerpo")
    })
    @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest body) {
        return authService.resetPassword(body).thenReturn(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Actualizar perfil del paciente en sesión")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado"),
            @ApiResponse(responseCode = "400", description = "Validación inválida"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación"),
            @ApiResponse(responseCode = "403", description = "Solo pacientes"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "Documento duplicado")
    })
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UserResponse>> updatePatientProfile(
            @Autenticacion UUID userId,
            @Valid @RequestPart("profile") PatientProfileUpdateRequest profile,
            @RequestPart(name = "avatar", required = false) FilePart avatar
    ) {
        return userService.updatePatientProfile(userId, profile, avatar).map(ResponseEntity::ok);
    }
}
