package com.app.hubble.mapper;

import com.app.hubble.dto.request.AppointmentRequest;
import com.app.hubble.entity.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {
    public Appointment toEntity(AppointmentRequest appointmentRequest) {
        return Appointment.builder()
                .id(appointmentRequest.getId())
                .active(appointmentRequest.isActive())
                .reason(appointmentRequest.getReason())
                .status(appointmentRequest.getStatus())
                .doctorId(appointmentRequest.getDoctorId())
                .patientId(appointmentRequest.getPatientId())
                .appointmentDate(appointmentRequest.getAppointmentDate())
                .build();
    }
}
