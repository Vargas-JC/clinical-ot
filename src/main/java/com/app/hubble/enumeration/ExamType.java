package com.app.hubble.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExamType {
    REFRACTION("REFRACTION"),
    FUNDUS("FUNDUS"),
    TONOMETRY("TONOMETRY");

    private final String code;
}
