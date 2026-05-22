package com.app.hubble.security;

import com.app.hubble.exception.UnauthorizedException;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Component
public class AutenticacionArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Autenticacion.class)
                && UUID.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Mono<Object> resolveArgument(
            MethodParameter parameter,
            BindingContext bindingContext,
            ServerWebExchange exchange) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    Authentication auth = ctx.getAuthentication();
                    if (auth == null || !auth.isAuthenticated()) {
                        return Mono.error(new UnauthorizedException("Sin sesión."));
                    }
                    Object principal = auth.getPrincipal();
                    if (principal instanceof String s) {
                        if (s.isEmpty() || "anonymousUser".equals(s)) {
                            return Mono.error(new UnauthorizedException("Sin sesión."));
                        }
                        try {
                            return Mono.just(UUID.fromString(s));
                        } catch (IllegalArgumentException ex) {
                            return Mono.error(new UnauthorizedException("Identidad de sesión inválida."));
                        }
                    }
                    return Mono.error(new UnauthorizedException("Identidad de sesión inválida."));
                })
                .cast(Object.class);
    }
}
