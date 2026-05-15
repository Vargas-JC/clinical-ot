package com.app.hubble.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionResponse {
    private UUID id;
    private UUID consultationId;
    private String description;
    private String lensType;
    private String recommendations;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
