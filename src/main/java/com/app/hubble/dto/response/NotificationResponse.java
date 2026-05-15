package com.app.hubble.dto.response;

import com.app.hubble.enumeration.NotificationType;
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
public class NotificationResponse {
    private UUID id;
    private UUID userId;
    private NotificationType type;
    private String title;
    private String body;
    private UUID appointmentId;
    private UUID paymentId;
    private LocalDateTime emailSentAt;
    private LocalDateTime appSentAt;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
