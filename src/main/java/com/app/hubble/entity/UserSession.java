package com.app.hubble.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table("user_sessions")
public class UserSession {
    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;

    @Column("refresh_token_hash")
    private String refreshTokenHash;

    @Column("access_token_jti")
    private String accessTokenJti;

    @Column("user_agent")
    private String userAgent;

    @Column("ip_address")
    private String ipAddress;

    @Column("issued_at")
    private LocalDateTime issuedAt;

    @Column("expires_at")
    private LocalDateTime expiresAt;

    @Column("last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column("revoked_at")
    private LocalDateTime revokedAt;
    private boolean active;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("deleted_at")
    private LocalDateTime deletedAt;
}
