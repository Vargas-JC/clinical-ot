package com.app.hubble.repository.row;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface PatientDetailRow {
    UUID getId();

    UUID getUserId();

    String getDocumentNumber();

    LocalDate getBirthDate();

    Boolean getActive();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    String getFullName();

    String getEmail();

    String getPhone();
}
