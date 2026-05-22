package com.app.hubble.controller;

import com.app.hubble.dto.request.UserRequest;
import com.app.hubble.dto.response.UserResponse;
import com.app.hubble.security.Autenticacion;
import com.app.hubble.service.UserService;
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

@Tag(name = "Usuarios", description = "Gestión de usuarios y perfiles del sistema.")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Listar usuarios")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de usuarios"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<UserResponse>>> findAll(
            @RequestParam(name = Pagination.defaultNamePage, defaultValue = Pagination.defaultPage) int page,
            @RequestParam(name = Pagination.defaultNameSize, defaultValue = Pagination.defaultSize) int size) {
        return userService.findAllUsers(page, size).map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtener usuario en sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil del usuario logeado"),
            @ApiResponse(responseCode = "401", description = "Sin autenticación")
    })
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UserResponse>> getCurrentUser(@Autenticacion UUID userId) {
        return userService.findUserById(userId).map(ResponseEntity::ok);
    }

    @Operation(summary = "Obtener usuario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UserResponse>> findById(@PathVariable UUID id) {
        return userService.findUserById(id).map(ResponseEntity::ok);
    }

    @Operation(summary = "Actualizar usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UserResponse>> update(@Valid @RequestBody UserRequest body) {
        return userService.updateUser(body).map(ResponseEntity::ok);
    }

    @Operation(summary = "Eliminar usuario (lógico)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return userService.deleteUserById(id).then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }
}
