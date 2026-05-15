package com.app.hubble.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PatientRequest {
    private UUID id;

    @NotNull(message = "El identificador de usuario es obligatorio.")
    private UUID userId;

    @NotBlank(message = "El número de documento es obligatorio.")
    @Size(max = 20, message = "El número de documento admite como máximo 20 caracteres.")
    private String documentNumber;

    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    private LocalDate birthDate;

    @NotBlank(message = "El nombre completo es obligatorio.")
    @Size(max = 100, message = "El nombre completo admite como máximo 100 caracteres.")
    private String fullName;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El correo electrónico no tiene un formato válido.")
    @Size(max = 150, message = "El correo admite como máximo 150 caracteres.")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio.")
    @Size(max = 20, message = "El teléfono admite como máximo 20 caracteres.")
    private String phone;
    private boolean active;
}
