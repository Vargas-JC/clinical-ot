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

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table("password_resets")
public class PasswordReset {
    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;

    private String code;

    @Column("expires_at")
    private LocalDateTime expiresAt;

    private boolean used;

    @Column("created_at")
    private LocalDateTime createdAt;
}
