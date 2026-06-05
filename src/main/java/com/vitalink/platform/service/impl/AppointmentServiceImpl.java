package com.vitalink.platform.service.impl;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.common.exception.BusinessException;
import com.vitalink.platform.common.exception.ResourceNotFoundException;
import com.vitalink.platform.dto.appointment.AppointmentRequest;
import com.vitalink.platform.dto.appointment.AppointmentRescheduleRequest;
import com.vitalink.platform.dto.appointment.AppointmentResponse;
import com.vitalink.platform.dto.appointment.AppointmentStatusUpdateRequest;
import com.vitalink.platform.entity.Appointment;
import com.vitalink.platform.entity.HealthcareProfessional;
import com.vitalink.platform.entity.InsurancePlan;
import com.vitalink.platform.entity.Organization;
import com.vitalink.platform.entity.Patient;
import com.vitalink.platform.entity.enums.AppointmentStatus;
import com.vitalink.platform.entity.enums.RecordStatus;
import com.vitalink.platform.mapper.AppointmentMapper;
import com.vitalink.platform.repository.AppointmentRepository;
import com.vitalink.platform.service.AppointmentService;
import com.vitalink.platform.service.InsurancePlanService;
import com.vitalink.platform.service.OrganizationService;
import com.vitalink.platform.service.PatientService;
import com.vitalink.platform.service.ProfessionalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentServiceImpl implements AppointmentService {
    private static final Set<AppointmentStatus> BLOCKING_STATUSES =
            EnumSet.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);

    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;
    private final ProfessionalService professionalService;
    private final OrganizationService organizationService;
    private final InsurancePlanService insurancePlanService;
    private final AppointmentMapper appointmentMapper;
    private final Clock clock;

    @Override
    @Transactional
    public AppointmentResponse schedule(AppointmentRequest request) {
        validatePeriod(request.getScheduledStart(), request.getScheduledEnd());

        Patient patient = patientService.getEntityOrThrow(request.getPatientId());
        requireActive(patient.getStatus(), "Paciente");

        HealthcareProfessional professional = professionalService.getEntityOrThrow(request.getProfessionalId());
        requireActive(professional.getStatus(), "Profissional");

        Organization organization = organizationService.getEntityOrThrow(request.getOrganizationId());

        InsurancePlan plan = resolveOptionalPlan(request.getInsurancePlanId());

        validateNoConflict(professional.getId(),
                request.getScheduledStart(), request.getScheduledEnd(), null);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .professional(professional)
                .organization(organization)
                .insurancePlan(plan)
                .scheduledStart(request.getScheduledStart())
                .scheduledEnd(request.getScheduledEnd())
                .type(request.getType())
                .reason(request.getReason())
                .status(AppointmentStatus.SCHEDULED)
                .build();

        appointment = appointmentRepository.save(appointment);
        log.info("Consulta agendada: id={}, profissional={}, inicio={}",
                appointment.getId(), professional.getId(), appointment.getScheduledStart());
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse reschedule(UUID id, AppointmentRescheduleRequest request) {
        Appointment appointment = getEntityOrThrow(id);

        if (!appointment.getStatus().blocksSchedule()) {
            throw new BusinessException(
                    "Apenas consultas agendadas ou confirmadas podem ser reagendadas (status atual: "
                            + appointment.getStatus() + ")");
        }

        validatePeriod(request.getScheduledStart(), request.getScheduledEnd());
        validateNoConflict(appointment.getProfessional().getId(),
                request.getScheduledStart(), request.getScheduledEnd(), appointment.getId());

        appointment.setScheduledStart(request.getScheduledStart());
        appointment.setScheduledEnd(request.getScheduledEnd());
        log.info("Consulta reagendada: id={}, novoInicio={}", id, request.getScheduledStart());
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse updateStatus(UUID id, AppointmentStatusUpdateRequest request) {
        Appointment appointment = getEntityOrThrow(id);
        AppointmentStatus current = appointment.getStatus();
        AppointmentStatus target = request.getStatus();

        if (current == target) {
            throw new BusinessException("A consulta ja esta no status " + target);
        }
        if (!current.canTransitionTo(target)) {
            throw new BusinessException(
                    String.format("Transicao de status invalida: %s -> %s", current, target));
        }

        appointment.setStatus(target);
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            appointment.setNotes(request.getNotes());
        }
        log.info("Status da consulta alterado: id={}, {} -> {}", id, current, target);
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    public AppointmentResponse getById(UUID id) {
        return appointmentMapper.toResponse(getEntityOrThrow(id));
    }

    @Override
    public PageResponse<AppointmentResponse> listByPatient(UUID patientId, Pageable pageable) {
        return PageResponse.from(
                appointmentRepository.findByPatientId(patientId, pageable).map(appointmentMapper::toResponse));
    }

    @Override
    public PageResponse<AppointmentResponse> listByProfessional(UUID professionalId, Pageable pageable) {
        return PageResponse.from(
                appointmentRepository.findByProfessionalId(professionalId, pageable).map(appointmentMapper::toResponse));
    }

    private Appointment getEntityOrThrow(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta", "id", id));
    }

    private void validatePeriod(OffsetDateTime start, OffsetDateTime end) {
        if (!end.isAfter(start)) {
            throw new BusinessException("O fim do agendamento deve ser posterior ao inicio");
        }
        if (start.isBefore(OffsetDateTime.now(clock))) {
            throw new BusinessException("Nao e possivel agendar em um horario no passado");
        }
    }

    private void validateNoConflict(UUID professionalId, OffsetDateTime start,
                                    OffsetDateTime end, UUID excludedId) {
        List<Appointment> conflicts = (excludedId == null)
                ? appointmentRepository.findConflicts(professionalId, start, end, BLOCKING_STATUSES)
                : appointmentRepository.findConflictsExcluding(professionalId, start, end, BLOCKING_STATUSES, excludedId);
        if (!conflicts.isEmpty()) {
            throw new BusinessException(
                    "O profissional ja possui uma consulta neste intervalo de horario");
        }
    }

    private InsurancePlan resolveOptionalPlan(UUID planId) {
        if (planId == null) {
            return null;
        }
        InsurancePlan plan = insurancePlanService.getEntityOrThrow(planId);
        if (plan.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException("O plano de saude informado esta inativo");
        }
        return plan;
    }

    private void requireActive(RecordStatus status, String actor) {
        if (status != RecordStatus.ACTIVE) {
            throw new BusinessException(actor + " esta inativo e nao pode participar de uma consulta");
        }
    }
}
