package com.app.hubble.repository.row;

import com.app.hubble.enumeration.SpecialityType;
import java.time.LocalDateTime;
import java.util.UUID;

public interface DoctorDetailRow {
    UUID getId();

    UUID getUserId();

    SpecialityType getSpecialty();

    Boolean getActive();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    String getFullName();

    String getEmail();

    String getPhone();
}
