package com.app.hubble.controller;

import com.app.hubble.dto.request.NotificationRequest;
import com.app.hubble.dto.response.NotificationResponse;
import com.app.hubble.security.Autenticacion;
import com.app.hubble.service.NotificationCatalogService;
import com.app.hubble.util.PageResponse;
import com.app.hubble.util.Pagination;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Tag(name = "Notificaciones (catálogo)", description = "Listado y mantenimiento de filas en notifications. El envío automático por correo sigue en otros flujos. Requiere JWT.")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class NotificationController {
    private final NotificationCatalogService notificationCatalogService;

    @Operation(summary = "Listar notificaciones")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de notificaciones"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<NotificationResponse>>> findAll(
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return notificationCatalogService.findAllNotifications(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Listar notificaciones del usuario en sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de notificaciones"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<NotificationResponse>>> getCurrentNotifications(
            @Autenticacion Mono<UUID> userId,
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return userId.flatMap(uid -> notificationCatalogService.findNotificationsForCurrentUser(uid, page, size))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtener notificación por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificación encontrada"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<NotificationResponse>> findById(@PathVariable UUID id) {
        return notificationCatalogService.findNotificationById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Crear notificación")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificación creada"),
            @ApiResponse(responseCode = "400", description = "Validación del cuerpo o reglas de negocio")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<NotificationResponse>> save(@Valid @RequestBody NotificationRequest body) {
        return notificationCatalogService.saveNotification(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Actualizar notificación")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificación actualizada"),
            @ApiResponse(responseCode = "400", description = "Id nulo, validación o reglas de negocio")
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<NotificationResponse>> update(@Valid @RequestBody NotificationRequest body) {
        return notificationCatalogService.updateNotification(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Eliminar notificación (lógico)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Borrado lógico aplicado"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return notificationCatalogService.deleteNotification(id).then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }
}
