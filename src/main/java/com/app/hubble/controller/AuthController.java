package com.app.hubble.controller;

import com.app.hubble.dto.auth.ForgotPasswordRequest;
import com.app.hubble.dto.auth.LoginRequest;
import com.app.hubble.dto.auth.RefreshRequest;
import com.app.hubble.dto.auth.RegisterRequest;
import com.app.hubble.dto.auth.ResetPasswordRequest;
import com.app.hubble.dto.auth.TokenResponse;
import com.app.hubble.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @Operation(summary = "Registro de paciente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens emitidos"),
            @ApiResponse(responseCode = "400", description = "Validación del cuerpo u otras reglas de negocio")
    })
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<TokenResponse>> register(
            @Valid @RequestBody RegisterRequest body,
            ServerWebExchange exchange
    ) {
        return authService.register(body, exchange).map(ResponseEntity::ok);
    }

    @Operation(summary = "Inicio de sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens emitidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o usuario inactivo")
    })
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<TokenResponse>> login(
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
    public Mono<ResponseEntity<TokenResponse>> refresh(
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
}
