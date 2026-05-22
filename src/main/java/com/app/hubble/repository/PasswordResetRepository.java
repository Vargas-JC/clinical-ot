package com.app.hubble.repository;

import com.app.hubble.entity.PasswordReset;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface PasswordResetRepository extends ReactiveCrudRepository<PasswordReset, UUID> {
    Flux<PasswordReset> findByUserIdAndUsedIsFalse(UUID userId);

    @Query("""
            SELECT pr.*
            FROM password_resets pr
            INNER JOIN users u ON pr.user_id = u.id
            WHERE u.email = :email
              AND u.deleted_at IS NULL
              AND TRIM(pr.code) = :code
              AND pr.used = FALSE
              AND pr.expires_at > :now
            LIMIT 1
            """)
    Mono<PasswordReset> findActiveByEmailAndCode(String email, String code, LocalDateTime now);
}
