package com.app.hubble.repository;

import com.app.hubble.entity.Prescription;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface PrescriptionRepository extends ReactiveCrudRepository<Prescription, UUID> {
    @Query("SELECT * FROM prescriptions WHERE deleted_at IS NULL ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<Prescription> findAllActivePaged(int limit, long offset);

    Mono<Long> countByDeletedAtIsNull();

    @Query("""
            SELECT pr.* FROM prescriptions pr
            INNER JOIN medical_consultations mc ON pr.consultation_id = mc.id AND mc.deleted_at IS NULL
            INNER JOIN appointments a ON mc.appointment_id = a.id AND a.deleted_at IS NULL
            INNER JOIN patients p ON a.patient_id = p.id AND p.deleted_at IS NULL
            WHERE pr.deleted_at IS NULL AND p.user_id = :userId
            ORDER BY pr.created_at DESC
            LIMIT :limit OFFSET :offset
            """)
    Flux<Prescription> findPagedForPatientUser(UUID userId, int limit, long offset);

    @Query("""
            SELECT COUNT(*)
            FROM prescriptions pr
            INNER JOIN medical_consultations mc ON pr.consultation_id = mc.id AND mc.deleted_at IS NULL
            INNER JOIN appointments a ON mc.appointment_id = a.id AND a.deleted_at IS NULL
            INNER JOIN patients p ON a.patient_id = p.id AND p.deleted_at IS NULL
            WHERE pr.deleted_at IS NULL AND p.user_id = :userId
            """)
    Mono<Long> countActiveForPatientUser(UUID userId);
}
