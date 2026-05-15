package com.app.hubble.config;

import com.app.hubble.brand.ClinicBranding;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(ClinicBranding.DISPLAY_NAME + " · API clínica")
                        .description("API reactiva del centro oftalmológico " + ClinicBranding.DISPLAY_NAME
                                + ": autenticación JWT, dominio clínico y notificaciones. "
                                + "Tras login, usa Authorize con `Bearer <accessToken>`.")
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Access JWT (RS256, clave privada en servidor). Emitido en /api/auth/login o /api/auth/refresh")));
    }
}
