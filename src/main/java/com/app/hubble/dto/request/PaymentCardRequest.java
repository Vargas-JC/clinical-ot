package com.app.hubble.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class PaymentCardRequest {
    private UUID id;

    @NotNull(message = "El identificador del usuario es obligatorio.")
    private UUID userId;

    @NotBlank(message = "El proveedor de pago es obligatorio.")
    @Size(max = 50, message = "El proveedor admite como máximo 50 caracteres.")
    private String provider;

    @NotBlank(message = "El token del proveedor es obligatorio.")
    @Size(max = 255, message = "El token admite como máximo 255 caracteres.")
    private String providerCardToken;

    @NotBlank(message = "La marca de la tarjeta es obligatoria.")
    @Size(max = 30, message = "La marca admite como máximo 30 caracteres.")
    private String brand;

    @NotBlank(message = "Los últimos cuatro dígitos son obligatorios.")
    @Size(min = 4, max = 4, message = "Los últimos cuatro dígitos deben tener exactamente 4 caracteres.")
    private String lastFour;

    @NotNull(message = "El mes de caducidad es obligatorio.")
    @Min(value = 1, message = "El mes de caducidad debe ser al menos 1.")
    @Max(value = 12, message = "El mes de caducidad no puede ser mayor que 12.")
    private Short expMonth;

    @NotNull(message = "El año de caducidad es obligatorio.")
    @Min(value = 2000, message = "El año de caducidad debe ser al menos 2000.")
    @Max(value = 2100, message = "El año de caducidad no puede ser mayor que 2100.")
    private Short expYear;

    @NotBlank(message = "El nombre del titular es obligatorio.")
    @Size(max = 100, message = "El nombre del titular admite como máximo 100 caracteres.")
    private String holderName;
    private boolean defaultCard;
    private boolean active;
}
