package com.app.hubble.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table("medical_consultations")
public class MedicalConsultation {
    @Id
    private UUID id;

    @Column("appointment_id")
    private UUID appointmentId;

    @Column("visual_acuity")
    private String visualAcuity;

    @Column("intraocular_pressure")
    private String intraocularPressure;
    private String diagnosis;
    private String notes;
    private boolean active;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("deleted_at")
    private LocalDateTime deletedAt;
}
