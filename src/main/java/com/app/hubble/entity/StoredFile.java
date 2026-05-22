package com.app.hubble.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table("stored_files")
public class StoredFile implements Persistable<UUID> {
    @Id
    private UUID id;

    @Transient
    @Builder.Default
    private boolean newStoredFile = false;

    @Column("original_name")
    private String originalName;

    @Column("content_type")
    private String contentType;

    @Column("size_bytes")
    private long sizeBytes;

    @Column("storage_path")
    private String storagePath;

    @Column("checksum_sha256")
    private String checksumSha256;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Override
    public boolean isNew() {
        return newStoredFile;
    }
}
