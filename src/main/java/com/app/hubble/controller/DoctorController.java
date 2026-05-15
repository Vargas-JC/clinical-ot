package com.app.hubble.controller;

import com.app.hubble.dto.request.DoctorRequest;
import com.app.hubble.dto.response.DoctorResponse;
import com.app.hubble.security.Autenticacion;
import com.app.hubble.service.DoctorService;
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

@Tag(name = "Doctores", description = "Perfil profesional en doctores."
)
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class DoctorController {
    private final DoctorService doctorService;

    @Operation(summary = "Listar doctores")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de doctores"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<DoctorResponse>>> findAll(
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return doctorService.findAllDoctors(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtener doctor en sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil de doctor"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<DoctorResponse>> getCurrentDoctor(@Autenticacion Mono<UUID> userId) {
        return userId.flatMap(doctorService::findDoctorProfileForCurrentUser).map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtener doctor por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doctor encontrado"),
            @ApiResponse(responseCode = "404", description = "Doctor inexistente o eliminado")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<DoctorResponse>> findById(@PathVariable UUID id) {
        return doctorService.findDoctorById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Crear perfil de doctor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil creado"),
            @ApiResponse(responseCode = "400", description = "Validación del cuerpo o reglas de negocio")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<DoctorResponse>> save(@Valid @RequestBody DoctorRequest body) {
        return doctorService.saveDoctor(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Actualizar doctor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado"),
            @ApiResponse(responseCode = "400", description = "Id nulo, validación o reglas de negocio")
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<DoctorResponse>> update(@Valid @RequestBody DoctorRequest body) {
        return doctorService.updateDoctor(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Eliminar doctor (lógico)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Borrado lógico aplicado"),
            @ApiResponse(responseCode = "404", description = "Doctor no encontrado")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return doctorService.deleteDoctor(id).then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }
}
