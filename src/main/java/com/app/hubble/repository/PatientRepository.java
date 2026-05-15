package com.app.hubble.repository;

import com.app.hubble.entity.Patient;
import com.app.hubble.repository.row.PatientDetailRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface PatientRepository extends ReactiveCrudRepository<Patient, UUID> {
    @Query("""
            SELECT
                p.id,
                p.user_id,
                p.document_number,
                p.birth_date,
                p.active,
                p.created_at,
                p.updated_at,
                u.full_name AS fullName,
                u.email AS email,
                u.phone AS phone
            FROM patients p
            INNER JOIN users u ON p.user_id = u.id
            WHERE p.deleted_at IS NULL AND u.deleted_at IS NULL
            ORDER BY p.created_at DESC
            LIMIT :limit OFFSET :offset
            """)
    Flux<PatientDetailRow> findAllPagedDetail(int limit, long offset);

    @Query("""
            SELECT
                p.id,
                p.user_id,
                p.document_number,
                p.birth_date,
                p.active,
                p.created_at,
                p.updated_at,
                u.full_name AS fullName,
                u.email AS email,
                u.phone AS phone
            FROM patients p
            INNER JOIN users u ON p.user_id = u.id
            WHERE p.id = :id AND p.deleted_at IS NULL AND u.deleted_at IS NULL
            LIMIT 1
            """)
    Mono<PatientDetailRow> findDetailById(UUID id);

    Mono<Patient> findByUserIdAndDeletedAtIsNull(UUID userId);

    Mono<Long> countByDeletedAtIsNull();
}
