package com.app.hubble.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hubble.app")
public record HubbleAppProperties(
        Integer passwordResetValidityMinutes,
        String storagePath,
        Long maxAvatarSizeBytes
) {
    public int passwordResetValidityMinutesResolved() {
        if (passwordResetValidityMinutes == null || passwordResetValidityMinutes < 1) {
            return 3;
        }
        return passwordResetValidityMinutes;
    }

    public String storagePathResolved() {
        if (storagePath == null || storagePath.isBlank()) {
            return "storage";
        }
        return storagePath.trim();
    }

    public long maxAvatarSizeBytesResolved() {
        if (maxAvatarSizeBytes == null || maxAvatarSizeBytes < 1) {
            return 5_242_880L;
        }
        return maxAvatarSizeBytes;
    }
}
