package com.app.hubble.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientProfileUpdateRequest {
    @NotBlank(message = "El nombre completo es obligatorio.")
    @Size(max = 100, message = "El nombre completo admite como máximo 100 caracteres.")
    private String fullName;

    @NotBlank(message = "El teléfono es obligatorio.")
    @Size(max = 20, message = "El teléfono admite como máximo 20 caracteres.")
    private String phone;

    @NotBlank(message = "El número de documento es obligatorio.")
    @Size(max = 20, message = "El número de documento admite como máximo 20 caracteres.")
    private String documentNumber;

    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    private LocalDate birthDate;

    private UUID avatarFileId;

    private boolean removeAvatar;
}
