package com.app.hubble.controller;

import com.app.hubble.dto.request.MedicalConsultationRequest;
import com.app.hubble.dto.response.MedicalConsultationResponse;
import com.app.hubble.security.Autenticacion;
import com.app.hubble.service.MedicalConsultationService;
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

@Tag(name = "Consultas médicas", description = "Registro clínico ligado a una cita. Requiere JWT.")
@RestController
@RequestMapping("/api/medical-consultations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class MedicalConsultationController {
    private final MedicalConsultationService medicalConsultationService;

    @Operation(summary = "Listar consultas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de consultas"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<MedicalConsultationResponse>>> findAll(
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return medicalConsultationService.findAllMedicalConsultations(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Listar consultas del usuario en sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de consultas"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<MedicalConsultationResponse>>> getCurrentMedicalConsultations(
            @Autenticacion Mono<UUID> userId,
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return userId.flatMap(uid -> medicalConsultationService.findMedicalConsultationsForCurrentUser(uid, page, size))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtener consulta por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta encontrada"),
            @ApiResponse(responseCode = "404", description = "Consulta no encontrada")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<MedicalConsultationResponse>> findById(@PathVariable UUID id) {
        return medicalConsultationService.findMedicalConsultationById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Crear consulta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta creada"),
            @ApiResponse(responseCode = "400", description = "Validación del cuerpo o reglas de negocio")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<MedicalConsultationResponse>> save(@Valid @RequestBody MedicalConsultationRequest body) {
        return medicalConsultationService.saveMedicalConsultation(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Actualizar consulta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta actualizada"),
            @ApiResponse(responseCode = "400", description = "Id nulo, validación o reglas de negocio")
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<MedicalConsultationResponse>> update(@Valid @RequestBody MedicalConsultationRequest body) {
        return medicalConsultationService.updateMedicalConsultation(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Eliminar consulta (lógico)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Borrado lógico aplicado"),
            @ApiResponse(responseCode = "404", description = "Consulta no encontrada")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return medicalConsultationService.deleteMedicalConsultation(id).then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }
}
