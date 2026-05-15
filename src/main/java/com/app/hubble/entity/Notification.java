package com.app.hubble.entity;

import com.app.hubble.enumeration.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table("notifications")
public class Notification {
    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;
    private NotificationType type;
    private String title;
    private String body;

    @Column("appointment_id")
    private UUID appointmentId;

    @Column("payment_id")
    private UUID paymentId;

    @Column("email_sent_at")
    private LocalDateTime emailSentAt;

    @Column("app_sent_at")
    private LocalDateTime appSentAt;
    private boolean active;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("deleted_at")
    private LocalDateTime deletedAt;
}
