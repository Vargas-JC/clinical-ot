package com.app.hubble.dto.response;

import com.app.hubble.enumeration.AppointmentStatus;
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
public class AppointmentResponse {
    private UUID id;
    private UUID patientId;
    private String patientName;
    private UUID doctorId;
    private String doctorName;
    private LocalDateTime appointmentDate;
    private AppointmentStatus status;
    private String reason;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
