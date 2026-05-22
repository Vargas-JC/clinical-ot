package com.app.hubble.repository;

import com.app.hubble.entity.Exam;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface ExamRepository extends ReactiveCrudRepository<Exam, UUID> {
    @Query("SELECT * FROM exams WHERE deleted_at IS NULL ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<Exam> findAllActivePaged(int limit, long offset);

    Mono<Long> countByDeletedAtIsNull();

    @Query("""
            SELECT * FROM exams
            WHERE deleted_at IS NULL AND user_id = :userId
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
            """)
    Flux<Exam> findPagedByUserId(UUID userId, int limit, long offset);

    @Query("SELECT COUNT(*) FROM exams WHERE deleted_at IS NULL AND user_id = :userId")
    Mono<Long> countByUserId(UUID userId);
}
