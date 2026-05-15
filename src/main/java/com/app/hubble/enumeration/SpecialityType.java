package com.app.hubble.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SpecialityType {
    GENERAL("GENERAL"),
    RETINA("RETINA"),
    GLAUCOMA("GLAUCOMA"),
    REFRACTIVE("REFRACTIVE");

    private final String code;
}
