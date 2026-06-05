package com.vitalink.platform.controller;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.dto.user.UserResponse;
import com.vitalink.platform.entity.enums.RoleName;
import com.vitalink.platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Consulta de usuarios e perfil autenticado")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Dados do usuario autenticado")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Busca um usuario por id (apenas ADMIN)")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista usuarios paginados (apenas ADMIN)")
    public ResponseEntity<PageResponse<UserResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(userService.list(pageable));
    }
}
