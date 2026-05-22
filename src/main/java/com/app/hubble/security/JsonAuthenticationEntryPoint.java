package com.app.hubble.security;

import com.app.hubble.util.ApiErrorWriter;
import com.app.hubble.util.AuthErrorMessages;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JsonAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        String message = AuthErrorMessages.fromAuthenticationException(ex);
        return ApiErrorWriter.write(exchange, HttpStatus.UNAUTHORIZED, message, objectMapper);
    }
}
