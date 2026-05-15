package com.app.hubble.repository;

import com.app.hubble.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, UUID> {
    @Query("""
            SELECT * FROM users
            WHERE LOWER(email) = LOWER(:email) AND deleted_at IS NULL
            LIMIT 1
            """)
    Mono<User> findActiveByEmail(String email);
}
