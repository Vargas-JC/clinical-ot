package com.app.hubble.repository;

import com.app.hubble.entity.User;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
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

    @Query("""
            SELECT * FROM users
            WHERE document_number = :documentNumber AND deleted_at IS NULL
            LIMIT 1
            """)
    Mono<User> findActiveByDocumentNumber(String documentNumber);

    @Query("""
            SELECT * FROM users
            WHERE deleted_at IS NULL
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
            """)
    Flux<User> findAllPaged(int limit, long offset);

    Mono<Long> countByDeletedAtIsNull();

    @Modifying
    @Query("""
            UPDATE users
            SET password_hash = :passwordHash, updated_at = :updatedAt
            WHERE id = :userId AND deleted_at IS NULL
            """)
    Mono<Integer> updatePasswordById(UUID userId, String passwordHash, LocalDateTime updatedAt);

    @Modifying
    @Query("""
            UPDATE users
            SET full_name = :fullName,
                phone = :phone,
                document_number = :documentNumber,
                birth_date = :birthDate,
                updated_at = :updatedAt
            WHERE id = :userId AND deleted_at IS NULL
            """)
    Mono<Integer> updatePatientProfileById(
            UUID userId,
            String fullName,
            String phone,
            String documentNumber,
            LocalDate birthDate,
            LocalDateTime updatedAt
    );

    @Modifying
    @Query("""
            UPDATE users
            SET full_name = :fullName,
                phone = :phone,
                document_number = :documentNumber,
                birth_date = :birthDate,
                avatar_file_id = :avatarFileId,
                updated_at = :updatedAt
            WHERE id = :userId AND deleted_at IS NULL
            """)
    Mono<Integer> updatePatientProfileWithAvatarById(
            UUID userId,
            String fullName,
            String phone,
            String documentNumber,
            LocalDate birthDate,
            UUID avatarFileId,
            LocalDateTime updatedAt
    );
}
