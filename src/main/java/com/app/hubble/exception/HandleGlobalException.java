package com.app.hubble.exception;

import com.app.hubble.util.ApiErrorResponse;
import com.app.hubble.util.AuthErrorMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.DecodingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import java.util.stream.Collectors;

@Order(-2)
@RestControllerAdvice
public class HandleGlobalException {
    public static final String ERROR_MESSAGE_ATTRIBUTE = "api.error.message";
    public static final String ERROR_STATUS_ATTRIBUTE = "api.error.status";

    private static final Logger log = LoggerFactory.getLogger(HandleGlobalException.class);

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleWebExchangeBindException(
            WebExchangeBindException ex,
            ServerWebExchange exchange
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(m -> m != null && !m.isBlank())
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "Datos de entrada no válidos.";
        }
        return respond(exchange, HttpStatus.BAD_REQUEST, message, ex);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleServerWebInputException(
            ServerWebInputException ex,
            ServerWebExchange exchange
    ) {
        String message = resolveServerWebInputMessage(ex);
        return respond(exchange, HttpStatus.BAD_REQUEST, message, ex);
    }

    @ExceptionHandler(DecodingException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleDecodingException(
            DecodingException ex,
            ServerWebExchange exchange
    ) {
        return respond(exchange, HttpStatus.BAD_REQUEST, AuthErrorMessages.INVALID_JSON, ex);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleBadCredentialsException(
            BadCredentialsException ex,
            ServerWebExchange exchange
    ) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = AuthErrorMessages.TOKEN_INVALID;
        }
        return respond(exchange, HttpStatus.UNAUTHORIZED, message, ex);
    }

    @ExceptionHandler(AuthenticationException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleAuthenticationException(
            AuthenticationException ex,
            ServerWebExchange exchange
    ) {
        return respond(exchange, HttpStatus.UNAUTHORIZED, AuthErrorMessages.fromAuthenticationException(ex), ex);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleAccessDeniedException(
            AccessDeniedException ex,
            ServerWebExchange exchange
    ) {
        return respond(exchange, HttpStatus.FORBIDDEN, AuthErrorMessages.ACCESS_DENIED, ex);
    }

    @ExceptionHandler(BadRequestException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleBadRequestException(
            BadRequestException exception,
            ServerWebExchange exchange
    ) {
        return respond(exchange, HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
    }

    @ExceptionHandler(ConflictException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleConflictException(
            ConflictException exception,
            ServerWebExchange exchange
    ) {
        return respond(exchange, HttpStatus.CONFLICT, exception.getMessage(), exception);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleDuplicateKeyException(
            DuplicateKeyException exception,
            ServerWebExchange exchange
    ) {
        return respond(exchange, HttpStatus.CONFLICT, duplicateKeyMessage(exception), exception);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception,
            ServerWebExchange exchange
    ) {
        return respond(exchange, HttpStatus.BAD_REQUEST, dataIntegrityMessage(exception), exception);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleUnauthorizedException(
            UnauthorizedException exception,
            ServerWebExchange exchange
    ) {
        return respond(exchange, HttpStatus.UNAUTHORIZED, exception.getMessage(), exception);
    }

    @ExceptionHandler(ForbiddenException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleForbiddenException(
            ForbiddenException exception,
            ServerWebExchange exchange
    ) {
        return respond(exchange, HttpStatus.FORBIDDEN, exception.getMessage(), exception);
    }

    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleNotFoundException(
            NotFoundException exception,
            ServerWebExchange exchange
    ) {
        return respond(exchange, HttpStatus.NOT_FOUND, exception.getMessage(), exception);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleInternalServerErrorException(
            InternalServerErrorException exception,
            ServerWebExchange exchange
    ) {
        return respond(exchange, HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleResponseStatusException(
            ResponseStatusException ex,
            ServerWebExchange exchange
    ) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason();
        if (message == null || message.isBlank() || isFrameworkMessage(message)) {
            message = messageForStatus(status);
        }
        return respond(exchange, status, message, ex);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleGenericException(
            Exception exception,
            ServerWebExchange exchange
    ) {
        return respond(exchange, HttpStatus.INTERNAL_SERVER_ERROR, AuthErrorMessages.INTERNAL_ERROR, exception);
    }

    private Mono<ResponseEntity<ApiErrorResponse>> respond(
            ServerWebExchange exchange,
            HttpStatus status,
            String message,
            Throwable cause
    ) {
        String safeMessage = message == null || message.isBlank() ? messageForStatus(status) : message;
        exchange.getAttributes().put(ERROR_MESSAGE_ATTRIBUTE, safeMessage);
        exchange.getAttributes().put(ERROR_STATUS_ATTRIBUTE, status.value());
        if (status.is5xxServerError()) {
            log.error("API {} {} -> {}", exchange.getRequest().getMethod(), exchange.getRequest().getURI().getPath(),
                    safeMessage, cause);
        } else {
            log.warn("API {} {} -> {} {}", exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI().getPath(), status.value(), safeMessage);
        }
        return Mono.just(ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ApiErrorResponse(status.value(), safeMessage)));
    }

    private static String dataIntegrityMessage(DataIntegrityViolationException exception) {
        String raw = exception.getMessage();
        if (raw == null && exception.getCause() != null) {
            raw = exception.getCause().getMessage();
        }
        if (raw != null && raw.contains("fk_user_stored_file")) {
            return "El archivo de avatar no es válido o no existe.";
        }
        return "No se pudo guardar el perfil por datos inconsistentes.";
    }

    private static String duplicateKeyMessage(DuplicateKeyException exception) {
        String raw = exception.getMessage();
        if (raw == null && exception.getCause() != null) {
            raw = exception.getCause().getMessage();
        }
        if (raw != null && raw.contains("uq_users_document")) {
            return "Número de documento ya registrado.";
        }
        if (raw != null && raw.contains("uq_users_email")) {
            return "Correo ya registrado.";
        }
        return "El registro ya existe.";
    }

    private static String resolveServerWebInputMessage(ServerWebInputException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof WebExchangeBindException bindException) {
            String validationMessage = bindException.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .filter(fieldMessage -> !fieldMessage.endsWith(": null"))
                    .collect(Collectors.joining("; "));
            if (!validationMessage.isBlank()) {
                return validationMessage;
            }
        }
        String message = ex.getReason();
        if (message == null || message.isBlank() || isFrameworkMessage(message)) {
            return AuthErrorMessages.INVALID_REQUEST;
        }
        return message;
    }

    private static boolean isFrameworkMessage(String message) {
        return message.startsWith("org.springframework")
                || message.contains("JSON parse error")
                || message.contains("Failed to read");
    }

    private static String messageForStatus(HttpStatus status) {
        if (status == HttpStatus.UNAUTHORIZED) {
            return AuthErrorMessages.SESSION_REQUIRED;
        }
        if (status == HttpStatus.FORBIDDEN) {
            return AuthErrorMessages.ACCESS_DENIED;
        }
        if (status == HttpStatus.NOT_FOUND) {
            return "Recurso no encontrado.";
        }
        if (status == HttpStatus.BAD_REQUEST) {
            return AuthErrorMessages.INVALID_REQUEST;
        }
        return AuthErrorMessages.INTERNAL_ERROR;
    }
}
