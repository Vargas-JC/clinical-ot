package com.app.hubble.mapper;

import com.app.hubble.dto.request.PatientRequest;
import com.app.hubble.dto.response.PatientResponse;
import com.app.hubble.entity.Patient;
import com.app.hubble.repository.row.PatientDetailRow;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {
    public PatientResponse toResponse(PatientDetailRow row) {
        return PatientResponse.builder()
                .id(row.getId())
                .userId(row.getUserId())
                .documentNumber(row.getDocumentNumber())
                .birthDate(row.getBirthDate())
                .fullName(row.getFullName())
                .email(row.getEmail())
                .phone(row.getPhone())
                .active(Boolean.TRUE.equals(row.getActive()))
                .createdAt(row.getCreatedAt())
                .updatedAt(row.getUpdatedAt())
                .build();
    }

    public Patient toPatientEntity(PatientRequest request) {
        return Patient.builder()
                .id(request.getId())
                .userId(request.getUserId())
                .documentNumber(request.getDocumentNumber())
                .birthDate(request.getBirthDate())
                .active(request.isActive())
                .build();
    }
}
