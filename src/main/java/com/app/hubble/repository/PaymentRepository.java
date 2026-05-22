package com.app.hubble.repository;

import com.app.hubble.entity.Payment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface PaymentRepository extends ReactiveCrudRepository<Payment, UUID> {
    @Query("""
            SELECT * FROM payments
            WHERE deleted_at IS NULL
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
            """)
    Flux<Payment> findAllPaged(int limit, long offset);

    @Query("SELECT COUNT(*) FROM payments WHERE deleted_at IS NULL")
    Mono<Long> countActive();

    @Query("""
            SELECT * FROM payments
            WHERE deleted_at IS NULL AND user_id = :userId
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
            """)
    Flux<Payment> findPagedByUserId(UUID userId, int limit, long offset);

    @Query("SELECT COUNT(*) FROM payments WHERE deleted_at IS NULL AND user_id = :userId")
    Mono<Long> countActiveByUserId(UUID userId);
}
