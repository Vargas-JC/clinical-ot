package com.app.hubble.entity;

import com.app.hubble.enumeration.AppointmentStatus;
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
@Table(name = "appointments")
public class Appointment {
    @Id
    private UUID id;

    @Column("patient_id")
    private UUID patientId;

    @Column("doctor_id")
    private UUID doctorId;

    @Column("appointment_date")
    private LocalDateTime appointmentDate;

    private AppointmentStatus status;
    private String reason;
    private boolean active;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("deleted_at")
    private LocalDateTime deletedAt;
}
