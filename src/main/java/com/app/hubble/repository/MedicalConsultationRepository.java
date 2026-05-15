package com.app.hubble.repository;

import com.app.hubble.entity.MedicalConsultation;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface MedicalConsultationRepository extends ReactiveCrudRepository<MedicalConsultation, UUID> {
    @Query("SELECT * FROM medical_consultations WHERE deleted_at IS NULL ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<MedicalConsultation> findAllActivePaged(int limit, long offset);

    Mono<Long> countByDeletedAtIsNull();

    @Query("""
            SELECT mc.* FROM medical_consultations mc
            INNER JOIN appointments a ON mc.appointment_id = a.id AND a.deleted_at IS NULL
            INNER JOIN patients p ON a.patient_id = p.id AND p.deleted_at IS NULL
            WHERE mc.deleted_at IS NULL AND p.user_id = :userId
            ORDER BY mc.created_at DESC
            LIMIT :limit OFFSET :offset
            """)
    Flux<MedicalConsultation> findPagedForPatientUser(UUID userId, int limit, long offset);

    @Query("""
            SELECT COUNT(*)
            FROM medical_consultations mc
            INNER JOIN appointments a ON mc.appointment_id = a.id AND a.deleted_at IS NULL
            INNER JOIN patients p ON a.patient_id = p.id AND p.deleted_at IS NULL
            WHERE mc.deleted_at IS NULL AND p.user_id = :userId
            """)
    Mono<Long> countActiveForPatientUser(UUID userId);
}
