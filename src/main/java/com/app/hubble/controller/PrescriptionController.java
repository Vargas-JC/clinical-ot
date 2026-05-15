package com.app.hubble.controller;

import com.app.hubble.dto.request.PrescriptionRequest;
import com.app.hubble.dto.response.PrescriptionResponse;
import com.app.hubble.security.Autenticacion;
import com.app.hubble.service.PrescriptionService;
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

@Tag(name = "Recetas", description = "Recetas ligadas a consulta médica. Requiere JWT.")
@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;

    @Operation(summary = "Listar recetas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de recetas"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<PrescriptionResponse>>> findAll(
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return prescriptionService.findAllPrescriptions(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Listar recetas del usuario en sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de recetas"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<PrescriptionResponse>>> getCurrentPrescriptions(
            @Autenticacion Mono<UUID> userId,
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return userId.flatMap(uid -> prescriptionService.findPrescriptionsForCurrentUser(uid, page, size)).map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtener receta por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receta encontrada"),
            @ApiResponse(responseCode = "404", description = "Receta no encontrada")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PrescriptionResponse>> findById(@PathVariable UUID id) {
        return prescriptionService.findPrescriptionById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Crear receta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receta creada"),
            @ApiResponse(responseCode = "400", description = "Validación del cuerpo o reglas de negocio")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PrescriptionResponse>> save(@Valid @RequestBody PrescriptionRequest body) {
        return prescriptionService.savePrescription(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Actualizar receta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receta actualizada"),
            @ApiResponse(responseCode = "400", description = "Id nulo, validación o reglas de negocio")
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PrescriptionResponse>> update(@Valid @RequestBody PrescriptionRequest body) {
        return prescriptionService.updatePrescription(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Eliminar receta (lógico)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Borrado lógico aplicado"),
            @ApiResponse(responseCode = "404", description = "Receta no encontrada")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return prescriptionService.deletePrescription(id).then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }
}
