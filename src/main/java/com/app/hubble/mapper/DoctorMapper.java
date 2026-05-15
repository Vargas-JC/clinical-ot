package com.app.hubble.mapper;

import com.app.hubble.dto.request.DoctorRequest;
import com.app.hubble.dto.response.DoctorResponse;
import com.app.hubble.entity.Doctor;
import com.app.hubble.repository.row.DoctorDetailRow;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {
    public DoctorResponse toResponse(DoctorDetailRow row) {
        return DoctorResponse.builder()
                .id(row.getId())
                .userId(row.getUserId())
                .fullName(row.getFullName())
                .email(row.getEmail())
                .phone(row.getPhone())
                .specialty(row.getSpecialty())
                .active(Boolean.TRUE.equals(row.getActive()))
                .createdAt(row.getCreatedAt())
                .updatedAt(row.getUpdatedAt())
                .build();
    }

    public Doctor toDoctorEntity(DoctorRequest request) {
        return Doctor.builder()
                .id(request.getId())
                .userId(request.getUserId())
                .specialty(request.getSpecialty())
                .active(request.isActive())
                .build();
    }
}
