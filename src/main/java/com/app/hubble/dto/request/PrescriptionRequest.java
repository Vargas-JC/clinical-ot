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
public class PrescriptionRequest {
    private UUID id;

    @NotNull(message = "El identificador de la consulta médica es obligatorio.")
    private UUID consultationId;

    @Size(max = 8000, message = "La descripción admite como máximo 8000 caracteres.")
    private String description;

    @Size(max = 100, message = "El tipo de lente admite como máximo 100 caracteres.")
    private String lensType;

    @Size(max = 8000, message = "Las recomendaciones admiten como máximo 8000 caracteres.")
    private String recommendations;
    private boolean active;
}
