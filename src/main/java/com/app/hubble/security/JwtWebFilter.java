package com.app.hubble.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtWebFilter implements WebFilter {
    private final ReactiveAuthenticationManager jwtReactiveAuthenticationManager;

    public JwtWebFilter(ReactiveAuthenticationManager jwtReactiveAuthenticationManager) {
        this.jwtReactiveAuthenticationManager = jwtReactiveAuthenticationManager;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || header.length() < 8 || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return chain.filter(exchange);
        }
        String token = header.substring(7).trim();
        if (token.isEmpty()) {
            return chain.filter(exchange);
        }
        return jwtReactiveAuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(null, token))
                .flatMap(auth -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)))
                .onErrorResume(ex -> chain.filter(exchange));
    }
}
