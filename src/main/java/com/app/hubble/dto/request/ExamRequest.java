package com.app.hubble.dto.request;

import com.app.hubble.enumeration.ExamType;
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
public class ExamRequest {
    private UUID id;

    @NotNull(message = "El identificador del paciente es obligatorio.")
    private UUID patientId;
    private UUID consultationId;

    @NotNull(message = "El tipo de examen es obligatorio.")
    private ExamType type;

    @Size(max = 8000, message = "El resultado admite como máximo 8000 caracteres.")
    private String result;
    private boolean active;
}
