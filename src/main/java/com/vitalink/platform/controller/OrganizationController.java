package com.vitalink.platform.controller;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.dto.organization.OrganizationRequest;
import com.vitalink.platform.dto.organization.OrganizationResponse;
import com.vitalink.platform.entity.enums.OrganizationType;
import com.vitalink.platform.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Organizacoes", description = "Cadastro de hospitais, clinicas e operadoras")
public class OrganizationController {
    private final OrganizationService organizationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria uma organizacao (apenas ADMIN)")
    public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody OrganizationRequest request,
                                                        UriComponentsBuilder uriBuilder) {
        OrganizationResponse created = organizationService.create(request);
        URI location = uriBuilder.path("/api/v1/organizations/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza uma organizacao (apenas ADMIN)")
    public ResponseEntity<OrganizationResponse> update(@PathVariable UUID id,
                                                       @Valid @RequestBody OrganizationRequest request) {
        return ResponseEntity.ok(organizationService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma organizacao por id")
    public ResponseEntity<OrganizationResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Lista organizacoes (filtro opcional por tipo)")
    public ResponseEntity<PageResponse<OrganizationResponse>> list(
            @RequestParam(required = false) OrganizationType type, Pageable pageable) {
        return ResponseEntity.ok(organizationService.list(type, pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inativa uma organizacao (soft-delete, apenas ADMIN)")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        organizationService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reativa uma organizacao inativada (apenas ADMIN)")
    public ResponseEntity<OrganizationResponse> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.activate(id));
    }
}
