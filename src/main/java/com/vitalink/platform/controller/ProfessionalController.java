package com.vitalink.platform.controller;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.dto.professional.ProfessionalRequest;
import com.vitalink.platform.dto.professional.ProfessionalResponse;
import com.vitalink.platform.service.ProfessionalService;
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
@RequestMapping("/api/v1/professionals")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Profissionais", description = "Cadastro de profissionais de saude")
public class ProfessionalController {
    private final ProfessionalService professionalService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL', 'CLINIC')")
    @Operation(summary = "Cadastra um profissional de saude")
    public ResponseEntity<ProfessionalResponse> create(@Valid @RequestBody ProfessionalRequest request,
                                                        UriComponentsBuilder uriBuilder) {
        ProfessionalResponse created = professionalService.create(request);
        URI location = uriBuilder.path("/api/v1/professionals/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL', 'CLINIC')")
    @Operation(summary = "Atualiza um profissional")
    public ResponseEntity<ProfessionalResponse> update(@PathVariable UUID id,
                                                       @Valid @RequestBody ProfessionalRequest request) {
        return ResponseEntity.ok(professionalService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um profissional por id")
    public ResponseEntity<ProfessionalResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(professionalService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Lista profissionais (filtro opcional por organizacao)")
    public ResponseEntity<PageResponse<ProfessionalResponse>> list(
            @RequestParam(required = false) UUID organizationId, Pageable pageable) {
        return ResponseEntity.ok(professionalService.list(organizationId, pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL', 'CLINIC')")
    @Operation(summary = "Inativa um profissional (soft-delete)")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        professionalService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
