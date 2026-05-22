package com.app.hubble.dto.request;

import com.app.hubble.enumeration.PaymentMethod;
import com.app.hubble.enumeration.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PaymentRequest {
    private UUID id;

    @NotNull(message = "El identificador del usuario es obligatorio.")
    private UUID userId;
    private UUID appointmentId;
    private UUID examId;
    private UUID prescriptionId;
    private UUID paymentCardId;

    @NotNull(message = "El importe es obligatorio.")
    private BigDecimal amount;

    @NotNull(message = "El método de pago es obligatorio.")
    private PaymentMethod method;
    private PaymentStatus status;
    private boolean active;
}
