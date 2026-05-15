package com.app.hubble.repository;

import com.app.hubble.entity.PaymentCard;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface PaymentCardRepository extends ReactiveCrudRepository<PaymentCard, UUID> {
    @Query("SELECT * FROM payment_cards WHERE deleted_at IS NULL ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<PaymentCard> findAllActivePaged(int limit, long offset);

    Mono<Long> countByDeletedAtIsNull();

    @Query("""
            SELECT * FROM payment_cards
            WHERE deleted_at IS NULL AND patient_id = :patientId
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
            """)
    Flux<PaymentCard> findPagedByPatientId(UUID patientId, int limit, long offset);

    @Query("SELECT COUNT(*) FROM payment_cards WHERE deleted_at IS NULL AND patient_id = :patientId")
    Mono<Long> countByPatientId(UUID patientId);
}
