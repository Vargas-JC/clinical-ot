package com.app.hubble.controller;

import com.app.hubble.dto.request.AppointmentRequest;
import com.app.hubble.dto.response.AppointmentResponse;
import com.app.hubble.security.Autenticacion;
import com.app.hubble.service.AppointmentService;
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

@Tag(name = "Citas", description = "Reservas entre paciente y doctor."
)
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @Operation(summary = "Listar citas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de citas"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<AppointmentResponse>>> findAll(
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return appointmentService.findAllAppointments(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Listar citas del usuario en sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de citas"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<AppointmentResponse>>> getCurrentAppointments(
            @Autenticacion UUID userId,
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return appointmentService.findAppointmentsForCurrentUser(userId, page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtener cita por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cita encontrada"),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AppointmentResponse>> findById(@PathVariable UUID id) {
        return appointmentService.findAppointmentById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Crear cita")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cita creada"),
            @ApiResponse(responseCode = "400", description = "Validación del cuerpo o reglas de negocio")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AppointmentResponse>> save(@Valid @RequestBody AppointmentRequest body) {
        return appointmentService.saveAppointment(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Actualizar cita")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cita actualizada"),
            @ApiResponse(responseCode = "400", description = "Id nulo, validación o reglas de negocio")
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AppointmentResponse>> update(@Valid @RequestBody AppointmentRequest body) {
        return appointmentService.updateAppointment(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Eliminar cita (lógico)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Borrado lógico aplicado"),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return appointmentService.deleteAppointment(id).then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }
}
