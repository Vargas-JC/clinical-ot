package com.app.hubble.config;

import com.app.hubble.security.AutenticacionArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

@Configuration
public class ReactiveArgumentResolverConfig implements WebFluxConfigurer {
    private final AutenticacionArgumentResolver autenticacionArgumentResolver;

    public ReactiveArgumentResolverConfig(AutenticacionArgumentResolver autenticacionArgumentResolver) {
        this.autenticacionArgumentResolver = autenticacionArgumentResolver;
    }

    @Override
    public void configureArgumentResolvers(@NonNull ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(autenticacionArgumentResolver);
    }
}
