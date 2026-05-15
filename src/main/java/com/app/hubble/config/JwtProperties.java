package com.app.hubble.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hubble.jwt")
public record JwtProperties(
        String rsaPrivateKeyPath,
        String rsaPublicKeyPath,
        int accessMinutes,
        int refreshDays
) {
}
