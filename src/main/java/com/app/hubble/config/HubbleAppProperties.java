package com.app.hubble.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hubble.app")
public record HubbleAppProperties(
        Integer passwordResetValidityMinutes
) {
    public int passwordResetValidityMinutesResolved() {
        if (passwordResetValidityMinutes == null || passwordResetValidityMinutes < 5) {
            return 60;
        }
        return passwordResetValidityMinutes;
    }
}
