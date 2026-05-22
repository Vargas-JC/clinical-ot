package com.app.hubble.dto.request;

import com.app.hubble.enumeration.SpecialityType;
import com.app.hubble.enumeration.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class UserRequest {
    private UUID id;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String email;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String fullName;

    @NotBlank(message = "El teléfono es obligatorio")
    private String phone;

    @NotNull(message = "El rol es obligatorio")
    private UserRole role;

    private boolean active;

    private String documentNumber;
    private LocalDate birthDate;
    private SpecialityType specialty;
}
