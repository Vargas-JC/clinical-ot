package com.app.hubble.dto.response;

import com.app.hubble.enumeration.SpecialityType;
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
public class DoctorResponse {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String email;
    private String phone;
    private SpecialityType specialty;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
