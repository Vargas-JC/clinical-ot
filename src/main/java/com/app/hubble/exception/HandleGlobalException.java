package com.app.hubble.exception;

import com.app.hubble.util.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;
import java.util.stream.Collectors;

@RestControllerAdvice
public class HandleGlobalException {
    private static final Logger log = LoggerFactory.getLogger(HandleGlobalException.class);

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleWebExchangeBindException(WebExchangeBindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(m -> m != null && !m.isBlank())
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "Datos de entrada no válidos.";
        }
        ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(BadRequestException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleBadRequestException(BadRequestException exception) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage()
        );

        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(ConflictException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleConflictException(ConflictException exception) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage()
        );
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleUnauthorizedException(UnauthorizedException exception) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                exception.getMessage()
        );
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(ForbiddenException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleForbiddenException(ForbiddenException exception) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                exception.getMessage()
        );
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleNotFoundException(ApiException exception) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage()
        );

        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleInternalServerErrorException(ApiException exception) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                exception.getMessage()
        );

        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleGenericException(Exception exception) {
        log.error("error no controlado: ", exception);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                exception.getMessage()
        );

        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
