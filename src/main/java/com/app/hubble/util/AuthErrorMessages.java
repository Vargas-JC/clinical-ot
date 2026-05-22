package com.app.hubble.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

public final class AuthErrorMessages {
    public static final String SESSION_REQUIRED = "Debe iniciar sesión.";
    public static final String ACCESS_DENIED = "No tiene permisos para realizar esta acción.";
    public static final String TOKEN_INVALID = "Token inválido.";
    public static final String TOKEN_EXPIRED = "El token ha expirado.";
    public static final String TOKEN_MISSING = "Token de autenticación ausente.";
    public static final String INTERNAL_ERROR = "Error interno del servidor.";
    public static final String INVALID_REQUEST = "Solicitud no válida.";
    public static final String INVALID_JSON = "El cuerpo de la solicitud no es válido.";

    private AuthErrorMessages() {
    }

    public static String fromAuthenticationException(AuthenticationException ex) {
        if (ex instanceof BadCredentialsException badCredentials) {
            String message = badCredentials.getMessage();
            if (message != null && !message.isBlank()) {
                return message;
            }
        }
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return SESSION_REQUIRED;
        }
        if (message.contains("Full authentication")
                || message.contains("Not Authenticated")
                || message.contains("An Authentication object")) {
            return SESSION_REQUIRED;
        }
        return SESSION_REQUIRED;
    }

    public static BadCredentialsException toBadCredentials(Throwable cause) {
        if (cause instanceof ExpiredJwtException) {
            return new BadCredentialsException(TOKEN_EXPIRED, cause);
        }
        if (cause instanceof JwtException) {
            return new BadCredentialsException(TOKEN_INVALID, cause);
        }
        if (cause instanceof BadCredentialsException badCredentials) {
            return badCredentials;
        }
        return new BadCredentialsException(TOKEN_INVALID, cause);
    }
}
