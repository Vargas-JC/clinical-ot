package com.app.hubble.controller;

import com.app.hubble.dto.request.ExamRequest;
import com.app.hubble.dto.response.ExamResponse;
import com.app.hubble.security.Autenticacion;
import com.app.hubble.service.ExamService;
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

@Tag(name = "Exámenes", description = "Exámenes oftalmológicos por paciente y opcionalmente por consulta.")
@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class ExamController {
    private final ExamService examService;

    @Operation(summary = "Listar exámenes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de exámenes"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<ExamResponse>>> findAll(
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return examService.findAllExams(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Listar exámenes del usuario en sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de exámenes"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<ExamResponse>>> getCurrentExams(
            @Autenticacion Mono<UUID> userId,
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return userId.flatMap(uid -> examService.findExamsForCurrentUser(uid, page, size)).map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtener examen por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Examen encontrado"),
            @ApiResponse(responseCode = "404", description = "Examen no encontrado")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ExamResponse>> findById(@PathVariable UUID id) {
        return examService.findExamById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Registrar examen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Examen creado"),
            @ApiResponse(responseCode = "400", description = "Validación del cuerpo o reglas de negocio")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ExamResponse>> save(@Valid @RequestBody ExamRequest body) {
        return examService.saveExam(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Actualizar examen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Examen actualizado"),
            @ApiResponse(responseCode = "400", description = "Id nulo, validación o reglas de negocio")
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ExamResponse>> update(@Valid @RequestBody ExamRequest body) {
        return examService.updateExam(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Eliminar examen (lógico)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Borrado lógico aplicado"),
            @ApiResponse(responseCode = "404", description = "Examen no encontrado")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return examService.deleteExam(id).then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }
}
