package com.app.hubble.controller;

import com.app.hubble.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Tag(name = "Archivos", description = "Descarga de archivos almacenados.")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class FileController {
    private final FileStorageService fileStorageService;

    @Operation(summary = "Obtener archivo por id")
    @GetMapping(value = "/{id}")
    public Mono<ResponseEntity<byte[]>> findById(@PathVariable UUID id) {
        return fileStorageService.readFile(id)
                .map(payload -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(payload.contentType()))
                        .body(payload.bytes()));
    }
}
