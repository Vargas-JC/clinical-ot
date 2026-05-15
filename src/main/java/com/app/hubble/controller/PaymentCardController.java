package com.app.hubble.controller;

import com.app.hubble.dto.request.PaymentCardRequest;
import com.app.hubble.dto.response.PaymentCardResponse;
import com.app.hubble.security.Autenticacion;
import com.app.hubble.service.PaymentCardService;
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

@Tag(name = "Tarjetas de pago", description = "Tokens de pasarela por paciente. La respuesta no incluye el token. Requiere JWT.")
@RestController
@RequestMapping("/api/payment-cards")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class PaymentCardController {
    private final PaymentCardService paymentCardService;

    @Operation(summary = "Listar tarjetas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de tarjetas"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<PaymentCardResponse>>> findAll(
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return paymentCardService.findAllPaymentCards(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Listar tarjetas del usuario en sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de tarjetas"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación o token inválido")
    })
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<PaymentCardResponse>>> getCurrentPaymentCards(
            @Autenticacion Mono<UUID> userId,
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return userId.flatMap(uid -> paymentCardService.findPaymentCardsForCurrentUser(uid, page, size)).map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtener tarjeta por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarjeta encontrada"),
            @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PaymentCardResponse>> findById(@PathVariable UUID id) {
        return paymentCardService.findPaymentCardById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Registrar tarjeta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarjeta creada"),
            @ApiResponse(responseCode = "400", description = "Validación del cuerpo o reglas de negocio")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PaymentCardResponse>> save(@Valid @RequestBody PaymentCardRequest body) {
        return paymentCardService.savePaymentCard(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Actualizar tarjeta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarjeta actualizada"),
            @ApiResponse(responseCode = "400", description = "Id nulo, validación o reglas de negocio")
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PaymentCardResponse>> update(@Valid @RequestBody PaymentCardRequest body) {
        return paymentCardService.updatePaymentCard(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Eliminar tarjeta (lógico)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Borrado lógico aplicado"),
            @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return paymentCardService.deletePaymentCard(id).then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }
}
