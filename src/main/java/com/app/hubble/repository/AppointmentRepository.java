package com.app.hubble.repository;

import com.app.hubble.entity.Appointment;
import com.app.hubble.repository.view.AppointmentView;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends ReactiveCrudRepository<Appointment, UUID> {
    @Query("""
                SELECT
                    a.id,
                    a.user_id AS userId,
                    pu.full_name AS patientName,
                    a.doctor_id AS doctorId,
                    du.full_name AS doctorName,
                    a.appointment_date AS appointmentDate,
                    a.status,
                    a.reason,
                    a.active,
                    a.created_at AS createdAt,
                    a.updated_at AS updatedAt
                FROM appointments a
                INNER JOIN users pu ON a.user_id = pu.id
                INNER JOIN users du ON a.doctor_id = du.id
                WHERE a.deleted_at IS NULL
                ORDER BY a.appointment_date DESC
                LIMIT :limit OFFSET :offset
            """)
    Flux<AppointmentView> findAllPaged(int limit, long offset);

    @Query("""
            SELECT
                a.id,
                a.user_id AS userId,
                pu.full_name AS patientName,
                a.doctor_id AS doctorId,
                du.full_name AS doctorName,
                a.appointment_date AS appointmentDate,
                a.status,
                a.reason,
                a.active,
                a.created_at AS createdAt,
                a.updated_at AS updatedAt
            FROM appointments a
            INNER JOIN users pu ON a.user_id = pu.id
            INNER JOIN users du ON a.doctor_id = du.id
            WHERE a.id = :id AND a.deleted_at IS NULL
            LIMIT 1
            """)
    Mono<AppointmentView> findAppointmentById(UUID id);

    @Query("SELECT COUNT(*) FROM appointments WHERE deleted_at IS NULL")
    Mono<Long> countActive();

    @Query("""
                SELECT
                    a.id,
                    a.user_id AS userId,
                    pu.full_name AS patientName,
                    a.doctor_id AS doctorId,
                    du.full_name AS doctorName,
                    a.appointment_date AS appointmentDate,
                    a.status,
                    a.reason,
                    a.active,
                    a.created_at AS createdAt,
                    a.updated_at AS updatedAt
                FROM appointments a
                INNER JOIN users pu ON a.user_id = pu.id
                INNER JOIN users du ON a.doctor_id = du.id
                WHERE a.deleted_at IS NULL AND a.user_id = :userId
                ORDER BY a.appointment_date DESC
                LIMIT :limit OFFSET :offset
            """)
    Flux<AppointmentView> findPagedByUserId(UUID userId, int limit, long offset);

    @Query("""
            SELECT COUNT(*)
            FROM appointments a
            WHERE a.deleted_at IS NULL AND a.user_id = :userId
            """)
    Mono<Long> countActiveByUserId(UUID userId);

    @Query("""
                SELECT
                    a.id,
                    a.user_id AS userId,
                    pu.full_name AS patientName,
                    a.doctor_id AS doctorId,
                    du.full_name AS doctorName,
                    a.appointment_date AS appointmentDate,
                    a.status,
                    a.reason,
                    a.active,
                    a.created_at AS createdAt,
                    a.updated_at AS updatedAt
                FROM appointments a
                INNER JOIN users pu ON a.user_id = pu.id
                INNER JOIN users du ON a.doctor_id = du.id
                WHERE a.deleted_at IS NULL AND a.doctor_id = :doctorId
                ORDER BY a.appointment_date DESC
                LIMIT :limit OFFSET :offset
            """)
    Flux<AppointmentView> findPagedByDoctorId(UUID doctorId, int limit, long offset);

    @Query("""
            SELECT COUNT(*)
            FROM appointments a
            WHERE a.deleted_at IS NULL AND a.doctor_id = :doctorId
            """)
    Mono<Long> countActiveByDoctorId(UUID doctorId);
}
