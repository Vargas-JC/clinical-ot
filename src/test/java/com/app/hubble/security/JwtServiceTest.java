package com.app.hubble.security;

import com.app.hubble.config.JwtProperties;
import com.app.hubble.enumeration.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "classpath:jwt/private.pem",
                "classpath:jwt/public.pem",
                15,
                30
        );
        jwtService = new JwtService(properties, new DefaultResourceLoader());
    }

    @Test
    void createAccessTokenIncluyeSubjectRoleYJti() {
        UUID userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        String jti = "session-jti-001";
        String token = jwtService.createAccessToken(userId, UserRole.PATIENT, jti);
        Claims claims = jwtService.parseAccessToken(token);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.getId()).isEqualTo(jti);
        assertThat(claims.get("role", String.class)).isEqualTo(UserRole.PATIENT.name());
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void parseAccessTokenRechazaTokenManipulado() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.createAccessToken(userId, UserRole.ADMIN, UUID.randomUUID().toString());
        String tamperedToken = token.substring(0, token.length() - 4) + "XXXX";
        org.junit.jupiter.api.Assertions.assertThrows(
                io.jsonwebtoken.JwtException.class,
                () -> jwtService.parseAccessToken(tamperedToken)
        );
    }
}
