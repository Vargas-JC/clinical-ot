package com.app.hubble.security;

import com.app.hubble.util.ApiErrorWriter;
import com.app.hubble.util.AuthErrorMessages;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtWebFilter implements WebFilter {
    private final ReactiveAuthenticationManager jwtReactiveAuthenticationManager;
    private final ObjectMapper objectMapper;

    public JwtWebFilter(
            ReactiveAuthenticationManager jwtReactiveAuthenticationManager,
            ObjectMapper objectMapper) {
        this.jwtReactiveAuthenticationManager = jwtReactiveAuthenticationManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || header.length() < 8 || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return chain.filter(exchange);
        }
        String token = header.substring(7).trim();
        if (token.isEmpty()) {
            return ApiErrorWriter.write(
                    exchange,
                    HttpStatus.UNAUTHORIZED,
                    AuthErrorMessages.TOKEN_MISSING,
                    objectMapper);
        }
        return jwtReactiveAuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(null, token))
                .flatMap(auth -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)))
                .onErrorResume(AuthenticationException.class, ex -> ApiErrorWriter.write(
                        exchange,
                        HttpStatus.UNAUTHORIZED,
                        AuthErrorMessages.fromAuthenticationException(ex),
                        objectMapper));
    }
}
