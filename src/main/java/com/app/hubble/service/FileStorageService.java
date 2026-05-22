package com.app.hubble.service;

import com.app.hubble.config.HubbleAppProperties;
import com.app.hubble.dto.response.StoredFileResponse;
import com.app.hubble.entity.StoredFile;
import com.app.hubble.exception.BadRequestException;
import com.app.hubble.exception.NotFoundException;
import com.app.hubble.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    private static final Set<String> ALLOWED_AVATAR_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final StoredFileRepository storedFileRepository;
    private final HubbleAppProperties appProperties;

    public Mono<StoredFileResponse> storeAvatar(FilePart filePart) {
        return readFilePart(filePart)
                .flatMap(payload -> Mono.fromCallable(() -> writeToDisk(payload))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(storedFileRepository::save)
                        .map(this::toResponse));
    }

    public Mono<FilePayload> readFile(UUID fileId) {
        return storedFileRepository.findById(fileId)
                .switchIfEmpty(Mono.error(new NotFoundException("Archivo no encontrado.")))
                .flatMap(meta -> Mono.fromCallable(() -> loadFromDisk(meta))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    public Mono<Void> assertFileExists(UUID fileId) {
        return storedFileRepository.findById(fileId)
                .switchIfEmpty(Mono.error(new NotFoundException("Archivo no encontrado.")))
                .then();
    }

    public String buildPublicUrl(UUID fileId) {
        if (fileId == null) {
            return "";
        }
        return "/api/files/" + fileId;
    }

    private Mono<AvatarUploadPayload> readFilePart(FilePart filePart) {
        String contentType = filePart.headers().getContentType() == null
                ? ""
                : filePart.headers().getContentType().toString();
        if (!ALLOWED_AVATAR_TYPES.contains(contentType)) {
            return Mono.error(new BadRequestException("El avatar debe ser JPEG, PNG o WEBP."));
        }
        return DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {
                    byte[] content = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(content);
                    DataBufferUtils.release(dataBuffer);
                    return content;
                })
                .flatMap(bytes -> {
                    if (bytes.length == 0) {
                        return Mono.error(new BadRequestException("El archivo de avatar está vacío."));
                    }
                    if (bytes.length > appProperties.maxAvatarSizeBytesResolved()) {
                        return Mono.error(new BadRequestException("El avatar supera el tamaño máximo permitido."));
                    }
                    String originalName = filePart.filename();
                    if (originalName == null || originalName.isBlank()) {
                        originalName = "avatar";
                    }
                    return Mono.just(new AvatarUploadPayload(originalName, contentType, bytes));
                });
    }

    private StoredFile writeToDisk(AvatarUploadPayload payload) throws IOException {
        UUID fileId = UUID.randomUUID();
        String extension = resolveExtension(payload.contentType());
        Path baseDir = Path.of(appProperties.storagePathResolved()).toAbsolutePath().normalize();
        Files.createDirectories(baseDir);
        String relativePath = fileId + extension;
        Path target = baseDir.resolve(relativePath);
        Files.write(target, payload.bytes(), StandardOpenOption.CREATE_NEW);
        return StoredFile.builder()
                .id(fileId)
                .newStoredFile(true)
                .originalName(payload.originalName())
                .contentType(payload.contentType())
                .sizeBytes(payload.bytes().length)
                .storagePath(relativePath)
                .checksumSha256(sha256Hex(payload.bytes()))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private FilePayload loadFromDisk(StoredFile meta) throws IOException {
        Path filePath = Path.of(appProperties.storagePathResolved(), meta.getStoragePath()).toAbsolutePath().normalize();
        if (!Files.exists(filePath)) {
            throw new NotFoundException("Archivo no encontrado en almacenamiento.");
        }
        byte[] bytes = Files.readAllBytes(filePath);
        return new FilePayload(meta.getContentType(), bytes);
    }

    private StoredFileResponse toResponse(StoredFile storedFile) {
        return StoredFileResponse.builder()
                .id(storedFile.getId())
                .url(buildPublicUrl(storedFile.getId()))
                .contentType(storedFile.getContentType())
                .sizeBytes(storedFile.getSizeBytes())
                .build();
    }

    private String resolveExtension(String contentType) {
        if ("image/png".equals(contentType)) {
            return ".png";
        }
        if ("image/webp".equals(contentType)) {
            return ".webp";
        }
        return ".jpg";
    }

    private String sha256Hex(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (Exception exception) {
            return "";
        }
    }

    private record AvatarUploadPayload(String originalName, String contentType, byte[] bytes) {
    }

    public record FilePayload(String contentType, byte[] bytes) {
    }
}
