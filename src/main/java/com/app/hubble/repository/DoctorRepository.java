package com.app.hubble.repository;

import com.app.hubble.entity.Doctor;
import com.app.hubble.repository.row.DoctorDetailRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface DoctorRepository extends ReactiveCrudRepository<Doctor, UUID> {
    Mono<Doctor> findByUserIdAndDeletedAtIsNull(UUID userId);

    Mono<Boolean> existsByUserIdAndDeletedAtIsNull(UUID userId);

    @Query("""
            SELECT
                d.id,
                d.user_id,
                d.specialty,
                d.active,
                d.created_at,
                d.updated_at,
                u.full_name AS fullName,
                u.email AS email,
                u.phone AS phone
            FROM doctors d
            INNER JOIN users u ON d.user_id = u.id
            WHERE d.deleted_at IS NULL AND u.deleted_at IS NULL
            ORDER BY d.created_at DESC
            LIMIT :limit OFFSET :offset
            """)
    Flux<DoctorDetailRow> findAllPagedDetail(int limit, long offset);

    @Query("""
            SELECT
                d.id,
                d.user_id,
                d.specialty,
                d.active,
                d.created_at,
                d.updated_at,
                u.full_name AS fullName,
                u.email AS email,
                u.phone AS phone
            FROM doctors d
            INNER JOIN users u ON d.user_id = u.id
            WHERE d.id = :id AND d.deleted_at IS NULL AND u.deleted_at IS NULL
            LIMIT 1
            """)
    Mono<DoctorDetailRow> findDetailById(UUID id);

    Mono<Long> countByDeletedAtIsNull();
}
