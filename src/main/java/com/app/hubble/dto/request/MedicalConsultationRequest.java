package com.app.hubble.dto.request;

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
public class MedicalConsultationRequest {
    private UUID id;

    @NotNull(message = "El identificador de la cita es obligatorio.")
    private UUID appointmentId;

    @Size(max = 50, message = "La agudeza visual admite como máximo 50 caracteres.")
    private String visualAcuity;

    @Size(max = 50, message = "La presión intraocular admite como máximo 50 caracteres.")
    private String intraocularPressure;

    @Size(max = 8000, message = "El diagnóstico admite como máximo 8000 caracteres.")
    private String diagnosis;

    @Size(max = 8000, message = "Las notas admiten como máximo 8000 caracteres.")
    private String notes;
    private boolean active;
}
