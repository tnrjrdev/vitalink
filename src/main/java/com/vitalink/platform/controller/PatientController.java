package com.vitalink.platform.controller;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.dto.patient.PatientRequest;
import com.vitalink.platform.dto.patient.PatientResponse;
import com.vitalink.platform.service.PatientService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Pacientes", description = "Cadastro de pacientes")
public class PatientController {
    private final PatientService patientService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL', 'CLINIC', 'PROFESSIONAL')")
    @Operation(summary = "Cadastra um paciente")
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody PatientRequest request,
                                                   UriComponentsBuilder uriBuilder) {
        PatientResponse created = patientService.create(request);
        URI location = uriBuilder.path("/api/v1/patients/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL', 'CLINIC', 'PROFESSIONAL')")
    @Operation(summary = "Atualiza um paciente")
    public ResponseEntity<PatientResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(patientService.update(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL', 'CLINIC', 'PROFESSIONAL')")
    @Operation(summary = "Busca um paciente por id")
    public ResponseEntity<PatientResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(patientService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL', 'CLINIC', 'PROFESSIONAL')")
    @Operation(summary = "Lista pacientes paginados")
    public ResponseEntity<PageResponse<PatientResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(patientService.list(pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inativa um paciente (soft-delete, apenas ADMIN)")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        patientService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
