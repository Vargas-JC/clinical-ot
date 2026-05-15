package com.app.hubble.controller;

import com.app.hubble.dto.request.PatientRequest;
import com.app.hubble.dto.response.PatientResponse;
import com.app.hubble.security.Autenticacion;
import com.app.hubble.service.PatientService;
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

@Tag(name = "Pacientes", description = "Ficha clínica ligada a usuarios.")
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class PatientController {
    private final PatientService patientService;

    @Operation(summary = "Listar pacientes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de pacientes"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<PatientResponse>>> findAll(
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return patientService.findAllPatients(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtener paciente en sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ficha de paciente"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PatientResponse>> getCurrentPatient(@Autenticacion Mono<UUID> userId) {
        return userId.flatMap(patientService::findPatientProfileForCurrentUser).map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtener paciente por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
            @ApiResponse(responseCode = "404", description = "Paciente inexistente o eliminado")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PatientResponse>> findById(@PathVariable UUID id) {
        return patientService.findPatientById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Crear ficha de paciente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ficha creada"),
            @ApiResponse(responseCode = "400", description = "Validación del cuerpo o reglas de negocio")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PatientResponse>> save(@Valid @RequestBody PatientRequest body) {
        return patientService.savePatient(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Actualizar paciente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ficha actualizada"),
            @ApiResponse(responseCode = "400", description = "Id nulo, validación o reglas de negocio")
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PatientResponse>> update(@Valid @RequestBody PatientRequest body) {
        return patientService.updatePatient(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Eliminar paciente (lógico)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Borrado lógico aplicado"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return patientService.deletePatientById(id).then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }
}
