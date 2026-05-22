package com.app.hubble.repository;

import com.app.hubble.entity.StoredFile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface StoredFileRepository extends ReactiveCrudRepository<StoredFile, UUID> {
}
