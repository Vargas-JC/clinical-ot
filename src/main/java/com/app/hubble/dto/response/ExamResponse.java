package com.app.hubble.dto.response;

import com.app.hubble.enumeration.ExamType;
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
public class ExamResponse {
    private UUID id;
    private UUID userId;
    private UUID consultationId;
    private ExamType type;
    private String result;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
