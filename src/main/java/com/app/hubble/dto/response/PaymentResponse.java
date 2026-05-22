package com.app.hubble.dto.response;

import com.app.hubble.enumeration.PaymentMethod;
import com.app.hubble.enumeration.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private UUID id;
    private UUID userId;
    private UUID appointmentId;
    private UUID examId;
    private UUID prescriptionId;
    private UUID paymentCardId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
