package com.app.hubble.repository;

import com.app.hubble.entity.UserSession;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends ReactiveCrudRepository<UserSession, UUID> {
    @Query("""
            SELECT * FROM user_sessions
            WHERE refresh_token_hash = :hash
              AND revoked_at IS NULL
              AND deleted_at IS NULL
            LIMIT 1
            """)
    Mono<UserSession> findActiveByRefreshTokenHash(String hash);

    Flux<UserSession> findByUserIdAndRevokedAtIsNullAndDeletedAtIsNull(UUID userId);
}
