package com.app.hubble.entity;

import com.app.hubble.enumeration.PaymentMethod;
import com.app.hubble.enumeration.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table("payments")
public class Payment {
    @Id
    private UUID id;

    @Column("patient_id")
    private UUID patientId;

    @Column("appointment_id")
    private UUID appointmentId;

    @Column("exam_id")
    private UUID examId;

    @Column("prescription_id")
    private UUID prescriptionId;

    @Column("payment_card_id")
    private UUID paymentCardId;

    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private boolean active;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("deleted_at")
    private LocalDateTime deletedAt;
}
