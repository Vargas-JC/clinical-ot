package com.app.hubble.security;

import com.app.hubble.config.JwtProperties;
import com.app.hubble.enumeration.UserRole;
import com.app.hubble.util.PemRsaKeys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties properties;
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public JwtService(JwtProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        Resource privateResource = resourceLoader.getResource(properties.rsaPrivateKeyPath());
        Resource publicResource = resourceLoader.getResource(properties.rsaPublicKeyPath());
        RSAPrivateKey priv;
        RSAPublicKey pub;
        try {
            if (privateResource.exists() && publicResource.exists()) {
                try (InputStream pin = privateResource.getInputStream(); InputStream qin = publicResource.getInputStream()) {
                    priv = PemRsaKeys.readPkcs8PrivateKey(pin);
                    pub = PemRsaKeys.readSpkiPublicKey(qin);
                }
            } else {
                KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
                gen.initialize(2048);
                KeyPair kp = gen.generateKeyPair();
                priv = (RSAPrivateKey) kp.getPrivate();
                pub = (RSAPublicKey) kp.getPublic();
                log.warn(
                        "JWT RSA: no se encontraron PEM en {} y {}. Se generó un par en memoria; los access token caducan al reiniciar la aplicación.",
                        properties.rsaPrivateKeyPath(),
                        properties.rsaPublicKeyPath()
                );
            }
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo inicializar el par RSA para JWT.", e);
        }
        this.privateKey = priv;
        this.publicKey = pub;
    }

    public String createAccessToken(UUID userId, UserRole role, String jti) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.accessMinutes() * 60L);
        return Jwts.builder()
                .id(jti)
                .subject(userId.toString())
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
