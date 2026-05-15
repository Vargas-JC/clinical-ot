package com.app.hubble.dto.request;

import com.app.hubble.enumeration.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NotificationRequest {
    private UUID id;

    @NotNull(message = "El identificador de usuario es obligatorio.")
    private UUID userId;
    private UUID appointmentId;
    private UUID paymentId;

    @NotNull(message = "El tipo de notificación es obligatorio.")
    private NotificationType type;

    @NotBlank(message = "El título es obligatorio.")
    @Size(max = 200, message = "El título admite como máximo 200 caracteres.")
    private String title;

    @NotBlank(message = "El cuerpo del mensaje es obligatorio.")
    @Size(max = 8000, message = "El cuerpo admite como máximo 8000 caracteres.")
    private String body;
    private boolean active;
}
