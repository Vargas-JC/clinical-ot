package com.app.hubble.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCardResponse {
    private UUID id;
    private UUID patientId;
    private String provider;
    private String brand;
    private String lastFour;
    private short expMonth;
    private short expYear;
    private String holderName;
    private boolean defaultForPatient;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
