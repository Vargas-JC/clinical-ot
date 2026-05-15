package com.app.hubble.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaymentMethod {
    CASH("CASH"),
    CARD("CARD"),
    INSURANCE("INSURANCE"),
    YAPE("YAPE");

    private final String code;
}
