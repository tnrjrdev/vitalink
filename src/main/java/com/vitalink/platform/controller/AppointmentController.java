package com.vitalink.platform.controller;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.dto.appointment.AppointmentRequest;
import com.vitalink.platform.dto.appointment.AppointmentRescheduleRequest;
import com.vitalink.platform.dto.appointment.AppointmentResponse;
import com.vitalink.platform.dto.appointment.AppointmentStatusUpdateRequest;
import com.vitalink.platform.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Consultas", description = "Agendamento e gestao de consultas")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL', 'CLINIC', 'PROFESSIONAL', 'PATIENT')")
    @Operation(summary = "Agenda uma nova consulta")
    public ResponseEntity<AppointmentResponse> schedule(@Valid @RequestBody AppointmentRequest request,
                                                         UriComponentsBuilder uriBuilder) {
        AppointmentResponse created = appointmentService.schedule(request);
        URI location = uriBuilder.path("/api/v1/appointments/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL', 'CLINIC', 'PROFESSIONAL', 'PATIENT')")
    @Operation(summary = "Reagenda uma consulta para um novo horario")
    public ResponseEntity<AppointmentResponse> reschedule(@PathVariable UUID id,
                                                          @Valid @RequestBody AppointmentRescheduleRequest request) {
        return ResponseEntity.ok(appointmentService.reschedule(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOSPITAL', 'CLINIC', 'PROFESSIONAL')")
    @Operation(summary = "Altera o status da consulta (confirmar, concluir, cancelar, no-show)")
    public ResponseEntity<AppointmentResponse> updateStatus(@PathVariable UUID id,
                                                            @Valid @RequestBody AppointmentStatusUpdateRequest request) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma consulta por id")
    public ResponseEntity<AppointmentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Lista consultas de um paciente")
    public ResponseEntity<PageResponse<AppointmentResponse>> listByPatient(@PathVariable UUID patientId,
                                                                           Pageable pageable) {
        return ResponseEntity.ok(appointmentService.listByPatient(patientId, pageable));
    }

    @GetMapping("/professional/{professionalId}")
    @Operation(summary = "Lista consultas de um profissional")
    public ResponseEntity<PageResponse<AppointmentResponse>> listByProfessional(@PathVariable UUID professionalId,
                                                                                Pageable pageable) {
        return ResponseEntity.ok(appointmentService.listByProfessional(professionalId, pageable));
    }
}
