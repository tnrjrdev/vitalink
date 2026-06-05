package com.vitalink.platform.controller;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.dto.insurance.InsurancePlanRequest;
import com.vitalink.platform.dto.insurance.InsurancePlanResponse;
import com.vitalink.platform.service.InsurancePlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/v1/insurance-plans")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Planos de Saude", description = "Cadastro de planos ofertados pelas operadoras")
public class InsurancePlanController {
    private final InsurancePlanService insurancePlanService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSURER')")
    @Operation(summary = "Cria um plano de saude")
    public ResponseEntity<InsurancePlanResponse> create(@Valid @RequestBody InsurancePlanRequest request,
                                                         UriComponentsBuilder uriBuilder) {
        InsurancePlanResponse created = insurancePlanService.create(request);
        URI location = uriBuilder.path("/api/v1/insurance-plans/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSURER')")
    @Operation(summary = "Atualiza um plano de saude")
    public ResponseEntity<InsurancePlanResponse> update(@PathVariable UUID id,
                                                        @Valid @RequestBody InsurancePlanRequest request) {
        return ResponseEntity.ok(insurancePlanService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um plano por id")
    public ResponseEntity<InsurancePlanResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(insurancePlanService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Lista planos (filtro opcional por operadora)")
    public ResponseEntity<PageResponse<InsurancePlanResponse>> list(
            @RequestParam(required = false) UUID operatorId, Pageable pageable) {
        return ResponseEntity.ok(insurancePlanService.list(operatorId, pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSURER')")
    @Operation(summary = "Inativa um plano de saude (soft-delete)")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        insurancePlanService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
