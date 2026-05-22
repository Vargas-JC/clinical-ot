package com.app.hubble.dto.response;

import com.app.hubble.enumeration.SpecialityType;
import com.app.hubble.enumeration.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private UserRole role;
    private boolean active;
    private String documentNumber;
    private LocalDate birthDate;
    private SpecialityType specialty;
    private UUID avatarFileId;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
