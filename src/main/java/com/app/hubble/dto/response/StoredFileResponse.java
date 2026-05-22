package com.app.hubble.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoredFileResponse {
    private UUID id;
    private String url;
    private String contentType;
    private long sizeBytes;
}
