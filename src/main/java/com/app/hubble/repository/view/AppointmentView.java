package com.app.hubble.repository.view;

import com.app.hubble.enumeration.AppointmentStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public interface AppointmentView {
    UUID getId();
    UUID getUserId();
    String getPatientName();
    UUID getDoctorId();
    String getDoctorName();
    LocalDateTime getAppointmentDate();
    AppointmentStatus getStatus();
    String getReason();
    Boolean getActive();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}