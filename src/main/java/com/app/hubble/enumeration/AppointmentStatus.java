package com.app.hubble.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AppointmentStatus {
    SCHEDULED("SCHEDULED"),
    CONFIRMED("CONFIRMED"),
    ATTENDED("ATTENDED"),
    CANCELLED("CANCELLED");

    private final String code;
}
