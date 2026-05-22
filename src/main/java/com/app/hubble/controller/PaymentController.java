package com.app.hubble.controller;

import com.app.hubble.dto.request.PaymentRequest;
import com.app.hubble.dto.response.PaymentResponse;
import com.app.hubble.security.Autenticacion;
import com.app.hubble.service.PaymentService;
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

@Tag(name = "Pagos", description = "Movimientos de cobro vinculados a paciente.")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class PaymentController {
    private final PaymentService paymentService;

    @Operation(summary = "Listar pagos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de pagos"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<PaymentResponse>>> findAll(
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return paymentService.findAllPayments(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Listar pagos del usuario en sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de pagos"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<PaymentResponse>>> getCurrentPayments(
            @Autenticacion UUID userId,
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return paymentService.findPaymentsForCurrentUser(userId, page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtener pago por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PaymentResponse>> findById(@PathVariable UUID id) {
        return paymentService.findPaymentById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Registrar pago")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago creado"),
            @ApiResponse(responseCode = "400", description = "Validación del cuerpo o reglas de negocio")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PaymentResponse>> save(@Valid @RequestBody PaymentRequest body) {
        return paymentService.savePayment(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Actualizar pago")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago actualizado"),
            @ApiResponse(responseCode = "400", description = "Id nulo, validación o reglas de negocio")
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PaymentResponse>> update(@Valid @RequestBody PaymentRequest body) {
        return paymentService.updatePayment(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Eliminar pago (lógico)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Borrado lógico aplicado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return paymentService.deletePayment(id).then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }
}
