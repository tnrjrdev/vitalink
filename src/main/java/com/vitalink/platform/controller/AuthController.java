package com.vitalink.platform.controller;

import com.vitalink.platform.dto.auth.AuthResponse;
import com.vitalink.platform.dto.auth.LoginRequest;
import com.vitalink.platform.dto.auth.RefreshTokenRequest;
import com.vitalink.platform.dto.auth.RegisterRequest;
import com.vitalink.platform.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacao", description = "Registro, login e renovacao de tokens")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registra um novo usuario (paciente ou profissional)")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica e emite access + refresh token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renova o access token a partir de um refresh token valido")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}
