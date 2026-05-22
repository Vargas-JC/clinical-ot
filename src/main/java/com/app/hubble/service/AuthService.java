package com.app.hubble.service;

import com.app.hubble.config.HubbleAppProperties;
import com.app.hubble.config.JwtProperties;
import com.app.hubble.dto.auth.ForgotPasswordRequest;
import com.app.hubble.dto.auth.LoginRequest;
import com.app.hubble.dto.auth.RefreshRequest;
import com.app.hubble.dto.auth.RegisterRequest;
import com.app.hubble.dto.auth.ResetPasswordRequest;
import com.app.hubble.dto.auth.AuthResponse;
import com.app.hubble.dto.auth.RefreshTokenResponse;
import com.app.hubble.entity.PasswordReset;
import com.app.hubble.entity.User;
import com.app.hubble.entity.UserSession;
import com.app.hubble.exception.BadRequestException;
import com.app.hubble.exception.UnauthorizedException;
import com.app.hubble.repository.PasswordResetRepository;
import com.app.hubble.repository.UserRepository;
import com.app.hubble.repository.UserSessionRepository;
import com.app.hubble.security.JwtService;
import com.app.hubble.util.HashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final HubbleAppProperties appProperties;
    private final NotificationService notificationService;
    private final UserService userService;

    @Transactional
    public Mono<AuthResponse> register(RegisterRequest request, ServerWebExchange exchange) {
        return userService.assertRegistrationAvailable(request.getEmail(), request.getDocumentNumber())
                .then(Mono.defer(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    User user = User.builder()
                            .email(request.getEmail().trim().toLowerCase())
                            .passwordHash(passwordEncoder.encode(request.getPassword()))
                            .fullName(request.getFullName().trim())
                            .phone(request.getPhone().trim())
                            .role(request.getRole())
                            .active(true)
                            .documentNumber(request.getDocumentNumber().trim())
                            .birthDate(request.getBirthDate())
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return userRepository.save(user)
                            .flatMap(savedUser -> notificationService.onUserRegistered(savedUser)
                                    .onErrorResume(e -> Mono.empty())
                                    .thenReturn(savedUser))
                            .flatMap(savedUser -> buildAuthResponse(savedUser, exchange));
                }));
    }

    public Mono<AuthResponse> login(LoginRequest request, ServerWebExchange exchange) {
        return userRepository.findActiveByEmail(request.getEmail().trim().toLowerCase())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPasswordHash()))
                .switchIfEmpty(Mono.error(new UnauthorizedException("Credenciales incorrectas")))
                .flatMap(user -> buildAuthResponse(user, exchange));
    }

    @Transactional
    public Mono<RefreshTokenResponse> refresh(RefreshRequest request, ServerWebExchange exchange) {
        String hash = HashUtils.sha256Hex(request.getRefreshToken());
        return userSessionRepository.findActiveByRefreshTokenHash(hash)
                .filter(s -> s.getExpiresAt().isAfter(LocalDateTime.now()))
                .switchIfEmpty(Mono.error(new UnauthorizedException("Sesión inválida")))
                .flatMap(old -> userRepository.findById(old.getUserId())
                        .filter(User::isActive)
                        .switchIfEmpty(Mono.error(new UnauthorizedException("Usuario inactivo")))
                        .flatMap(user -> {
                            LocalDateTime now = LocalDateTime.now();
                            UserSession revoked = old.toBuilder().revokedAt(now).updatedAt(now).build();
                            return userSessionRepository.save(revoked).then(buildRefreshResponse(user, exchange));
                        }));
    }

    public Mono<Void> logout(RefreshRequest request) {
        String hash = HashUtils.sha256Hex(request.getRefreshToken());
        return userSessionRepository.findActiveByRefreshTokenHash(hash)
                .flatMap(session -> {
                    LocalDateTime now = LocalDateTime.now();
                    UserSession updated = session.toBuilder().revokedAt(now).updatedAt(now).build();
                    return userSessionRepository.save(updated);
                })
                .then();
    }

    public Mono<Void> forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        return userRepository.findActiveByEmail(email)
                .flatMap(this::issuePasswordResetAndSendEmail)
                .then();
    }

    private Mono<Void> issuePasswordResetAndSendEmail(User user) {
        LocalDateTime now = LocalDateTime.now();
        int validity = appProperties.passwordResetValidityMinutesResolved();
        return invalidateOpenPasswordResets(user.getId())
                .then(Mono.defer(() -> {
                    String code = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
                    PasswordReset row = PasswordReset.builder()
                            .userId(user.getId())
                            .code(code)
                            .expiresAt(now.plusMinutes(validity))
                            .used(false)
                            .createdAt(now)
                            .build();
                    return passwordResetRepository.save(row)
                            .flatMap(saved -> notificationService.sendPasswordResetOtpEmail(
                                    user.getEmail(),
                                    user.getFullName(),
                                    code,
                                    validity
                            ));
                }));
    }

    private Mono<Void> invalidateOpenPasswordResets(UUID userId) {
        return passwordResetRepository.findByUserIdAndUsedIsFalse(userId)
                .flatMap(reset -> {
                    reset.setUsed(true);
                    return passwordResetRepository.save(reset);
                })
                .then();
    }

    public Mono<Void> resetPassword(ResetPasswordRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String email = request.getEmail().trim().toLowerCase();
        String code = request.getCode().trim();
        return passwordResetRepository.findActiveByEmailAndCode(email, code, now)
                .switchIfEmpty(Mono.error(new BadRequestException("Código de recuperación inválido o caducado.")))
                .flatMap(pr -> userRepository.findById(pr.getUserId())
                        .filter(User::isActive)
                        .switchIfEmpty(Mono.error(new BadRequestException("Usuario no disponible.")))
                        .flatMap(user -> {
                            pr.setUsed(true);
                            String passwordHash = passwordEncoder.encode(request.getNewPassword());
                            return userRepository.updatePasswordById(user.getId(), passwordHash, now)
                                    .then(passwordResetRepository.save(pr))
                                    .then(revokeAllSessions(user.getId(), now));
                        }));
    }

    private Mono<Void> revokeAllSessions(UUID userId, LocalDateTime now) {
        return userSessionRepository.findByUserIdAndRevokedAtIsNullAndDeletedAtIsNull(userId)
                .flatMap(s -> userSessionRepository.save(s.toBuilder().revokedAt(now).updatedAt(now).build()))
                .then();
    }

    private Mono<AuthResponse> buildAuthResponse(User user, ServerWebExchange exchange) {
        return issueTokens(user, exchange)
                .map(tokens -> AuthResponse.builder()
                        .accessToken(tokens.accessToken())
                        .refreshToken(tokens.refreshToken())
                        .tokenType("Bearer")
                        .expiresIn(tokens.expiresIn())
                        .user(userService.toResponse(user))
                        .build());
    }

    private Mono<RefreshTokenResponse> buildRefreshResponse(User user, ServerWebExchange exchange) {
        return issueTokens(user, exchange)
                .map(tokens -> RefreshTokenResponse.builder()
                        .accessToken(tokens.accessToken())
                        .refreshToken(tokens.refreshToken())
                        .tokenType("Bearer")
                        .expiresIn(tokens.expiresIn())
                        .build());
    }

    private Mono<IssuedTokens> issueTokens(User user, ServerWebExchange exchange) {
        LocalDateTime now = LocalDateTime.now();
        String jti = UUID.randomUUID().toString();
        String accessToken = jwtService.createAccessToken(user.getId(), user.getRole(), jti);
        byte[] raw = new byte[48];
        SECURE_RANDOM.nextBytes(raw);
        String refreshPlain = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        String refreshHash = HashUtils.sha256Hex(refreshPlain);
        LocalDateTime expiresAt = now.plusDays(jwtProperties.refreshDays());
        UserSession session = UserSession.builder()
                .userId(user.getId())
                .refreshTokenHash(refreshHash)
                .accessTokenJti(jti)
                .userAgent(readUserAgent(exchange))
                .ipAddress(readIp(exchange))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .lastActivityAt(now)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        long expiresIn = jwtProperties.accessMinutes() * 60L;
        return userSessionRepository.save(session)
                .map(s -> new IssuedTokens(accessToken, refreshPlain, expiresIn));
    }

    private record IssuedTokens(String accessToken, String refreshToken, long expiresIn) {
    }

    private String readUserAgent(ServerWebExchange exchange) {
        String ua = exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT);
        if (ua == null || ua.isEmpty()) {
            return "unknown";
        }
        return ua.length() > 512 ? ua.substring(0, 512) : ua;
    }

    private String readIp(ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        if (exchange.getRequest().getRemoteAddress() != null
                && exchange.getRequest().getRemoteAddress().getAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        return "0.0.0.0";
    }
}
