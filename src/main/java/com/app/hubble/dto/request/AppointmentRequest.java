package com.app.hubble.dto.request;

import com.app.hubble.enumeration.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AppointmentRequest {
    private UUID id;

    @NotNull(message = "El identificador del paciente es obligatorio.")
    private UUID patientId;

    @NotNull(message = "El identificador del doctor es obligatorio.")
    private UUID doctorId;

    @NotNull(message = "La fecha y hora de la cita son obligatorias.")
    private LocalDateTime appointmentDate;
    private AppointmentStatus status;

    @Size(max = 8000, message = "El motivo admite como máximo 8000 caracteres.")
    private String reason;
    private boolean active;
}
