package com.app.hubble.security;

import com.app.hubble.util.AuthErrorMessages;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {
    private final JwtService jwtService;

    public JwtReactiveAuthenticationManager(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        Object credentials = authentication.getCredentials();
        if (!(credentials instanceof String token) || token.isEmpty()) {
            return Mono.error(new BadCredentialsException(AuthErrorMessages.TOKEN_MISSING));
        }
        return Mono.fromCallable(() -> jwtService.parseAccessToken(token))
                .subscribeOn(Schedulers.boundedElastic())
                .map(claims -> {
                    String sub = claims.getSubject();
                    String role = claims.get("role", String.class);
                    String authority = role == null ? "ROLE_PATIENT" : "ROLE_" + role;
                    return (Authentication) new UsernamePasswordAuthenticationToken(
                            sub,
                            null,
                            java.util.List.of(new SimpleGrantedAuthority(authority))
                    );
                })
                .onErrorMap(AuthErrorMessages::toBadCredentials);
    }
}
