package com.vitalink.platform.service;

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
import com.vitalink.platform.entity.enums.AppointmentType;
import com.vitalink.platform.entity.enums.RecordStatus;
import com.vitalink.platform.mapper.AppointmentMapper;
import com.vitalink.platform.repository.AppointmentRepository;
import com.vitalink.platform.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentServiceImpl")
class AppointmentServiceImplTest {
    private static final OffsetDateTime NOW = OffsetDateTime.of(2026, 6, 3, 10, 0, 0, 0, ZoneOffset.UTC);

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientService patientService;
    @Mock private ProfessionalService professionalService;
    @Mock private OrganizationService organizationService;
    @Mock private InsurancePlanService insurancePlanService;
    @Mock private EmailService emailService;
    @Mock private EventPublisher eventPublisher;

    private AppointmentServiceImpl service;

    private Patient patient;
    private HealthcareProfessional professional;
    private Organization organization;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW.toInstant(), ZoneOffset.UTC);
        service = new AppointmentServiceImpl(
                appointmentRepository, patientService, professionalService,
                organizationService, insurancePlanService, new AppointmentMapper(), fixedClock,
                emailService, eventPublisher);

        patient = Patient.builder().fullName("Joao").status(RecordStatus.ACTIVE).build();
        patient.setId(UUID.randomUUID());

        organization = Organization.builder().legalName("Hospital X").status(RecordStatus.ACTIVE).build();
        organization.setId(UUID.randomUUID());

        professional = HealthcareProfessional.builder()
                .fullName("Dra. Ana").organization(organization).status(RecordStatus.ACTIVE).build();
        professional.setId(UUID.randomUUID());
    }

    private AppointmentRequest validRequest() {
        AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(patient.getId());
        request.setProfessionalId(professional.getId());
        request.setOrganizationId(organization.getId());
        request.setScheduledStart(NOW.plusDays(1));
        request.setScheduledEnd(NOW.plusDays(1).plusMinutes(30));
        request.setType(AppointmentType.IN_PERSON);
        request.setReason("Consulta de rotina");
        return request;
    }

    private void stubActiveActors() {
        when(patientService.getEntityOrThrow(patient.getId())).thenReturn(patient);
        when(professionalService.getEntityOrThrow(professional.getId())).thenReturn(professional);
        when(organizationService.getEntityOrThrow(organization.getId())).thenReturn(organization);
    }

    @Nested
    @DisplayName("schedule")
    class Schedule {
        @Test
        @DisplayName("agenda com sucesso quando nao ha conflito")
        void shouldSchedule() {
            stubActiveActors();
            when(appointmentRepository.findConflicts(any(), any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(appointmentRepository.save(any(Appointment.class)))
                    .thenAnswer(invocation -> {
                        Appointment a = invocation.getArgument(0);
                        a.setId(UUID.randomUUID());
                        return a;
                    });

            AppointmentResponse response = service.schedule(validRequest());

            assertThat(response.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
            assertThat(response.getPatientId()).isEqualTo(patient.getId());
            verify(appointmentRepository).save(any(Appointment.class));
        }

        @Test
        @DisplayName("falha quando fim nao e posterior ao inicio")
        void shouldFailWhenEndBeforeStart() {
            AppointmentRequest request = validRequest();
            request.setScheduledEnd(request.getScheduledStart());

            assertThatThrownBy(() -> service.schedule(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("posterior ao inicio");
            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("falha quando o horario esta no passado")
        void shouldFailWhenInThePast() {
            AppointmentRequest request = validRequest();
            request.setScheduledStart(NOW.minusHours(1));
            request.setScheduledEnd(NOW.minusMinutes(30));

            assertThatThrownBy(() -> service.schedule(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("passado");
        }

        @Test
        @DisplayName("falha quando ha conflito de agenda do profissional")
        void shouldFailWhenConflict() {
            stubActiveActors();
            when(appointmentRepository.findConflicts(any(), any(), any(), any()))
                    .thenReturn(List.of(new Appointment()));

            assertThatThrownBy(() -> service.schedule(validRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ja possui uma consulta");
            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("falha quando o paciente esta inativo")
        void shouldFailWhenPatientInactive() {
            patient.setStatus(RecordStatus.INACTIVE);
            when(patientService.getEntityOrThrow(patient.getId())).thenReturn(patient);

            assertThatThrownBy(() -> service.schedule(validRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Paciente esta inativo");
        }

        @Test
        @DisplayName("falha quando o profissional esta inativo")
        void shouldFailWhenProfessionalInactive() {
            professional.setStatus(RecordStatus.INACTIVE);
            when(patientService.getEntityOrThrow(patient.getId())).thenReturn(patient);
            when(professionalService.getEntityOrThrow(professional.getId())).thenReturn(professional);

            assertThatThrownBy(() -> service.schedule(validRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Profissional esta inativo");
        }

        @Test
        @DisplayName("falha quando o plano informado esta inativo")
        void shouldFailWhenPlanInactive() {
            stubActiveActors();
            InsurancePlan plan = InsurancePlan.builder().name("Plano X").status(RecordStatus.INACTIVE).build();
            UUID planId = UUID.randomUUID();
            plan.setId(planId);

            AppointmentRequest request = validRequest();
            request.setInsurancePlanId(planId);
            when(insurancePlanService.getEntityOrThrow(planId)).thenReturn(plan);

            assertThatThrownBy(() -> service.schedule(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("plano de saude informado esta inativo");
        }
    }

    @Nested
    @DisplayName("reschedule")
    class Reschedule {
        @Test
        @DisplayName("reagenda consulta agendada")
        void shouldReschedule() {
            Appointment appointment = buildScheduledAppointment(AppointmentStatus.SCHEDULED);
            when(appointmentRepository.findById(appointment.getId())).thenReturn(java.util.Optional.of(appointment));
            when(appointmentRepository.findConflictsExcluding(any(), any(), any(), any(), eq(appointment.getId())))
                    .thenReturn(Collections.emptyList());

            AppointmentRescheduleRequest request = new AppointmentRescheduleRequest();
            request.setScheduledStart(NOW.plusDays(2));
            request.setScheduledEnd(NOW.plusDays(2).plusMinutes(30));

            AppointmentResponse response = service.reschedule(appointment.getId(), request);

            assertThat(response.getScheduledStart()).isEqualTo(NOW.plusDays(2));
        }

        @Test
        @DisplayName("falha ao reagendar consulta em status terminal")
        void shouldFailReschedulingTerminal() {
            Appointment appointment = buildScheduledAppointment(AppointmentStatus.COMPLETED);
            when(appointmentRepository.findById(appointment.getId())).thenReturn(java.util.Optional.of(appointment));

            AppointmentRescheduleRequest request = new AppointmentRescheduleRequest();
            request.setScheduledStart(NOW.plusDays(2));
            request.setScheduledEnd(NOW.plusDays(2).plusMinutes(30));

            assertThatThrownBy(() -> service.reschedule(appointment.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("podem ser reagendadas");
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {
        @Test
        @DisplayName("confirma consulta agendada")
        void shouldConfirm() {
            Appointment appointment = buildScheduledAppointment(AppointmentStatus.SCHEDULED);
            when(appointmentRepository.findById(appointment.getId())).thenReturn(java.util.Optional.of(appointment));

            AppointmentStatusUpdateRequest request = new AppointmentStatusUpdateRequest();
            request.setStatus(AppointmentStatus.CONFIRMED);

            AppointmentResponse response = service.updateStatus(appointment.getId(), request);

            assertThat(response.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        }

        @Test
        @DisplayName("rejeita transicao invalida")
        void shouldRejectInvalidTransition() {
            Appointment appointment = buildScheduledAppointment(AppointmentStatus.COMPLETED);
            when(appointmentRepository.findById(appointment.getId())).thenReturn(java.util.Optional.of(appointment));

            AppointmentStatusUpdateRequest request = new AppointmentStatusUpdateRequest();
            request.setStatus(AppointmentStatus.SCHEDULED);

            assertThatThrownBy(() -> service.updateStatus(appointment.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Transicao de status invalida");
        }

        @Test
        @DisplayName("rejeita transicao para o mesmo status")
        void shouldRejectSameStatus() {
            Appointment appointment = buildScheduledAppointment(AppointmentStatus.SCHEDULED);
            when(appointmentRepository.findById(appointment.getId())).thenReturn(java.util.Optional.of(appointment));

            AppointmentStatusUpdateRequest request = new AppointmentStatusUpdateRequest();
            request.setStatus(AppointmentStatus.SCHEDULED);

            assertThatThrownBy(() -> service.updateStatus(appointment.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ja esta no status");
        }
    }

    @Test
    @DisplayName("getById lanca 404 quando nao existe")
    void shouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(appointmentRepository.findById(id)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getById retorna a consulta mapeada")
    void shouldGetById() {
        Appointment appointment = buildScheduledAppointment(AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById(appointment.getId())).thenReturn(java.util.Optional.of(appointment));

        AppointmentResponse response = service.getById(appointment.getId());

        assertThat(response.getId()).isEqualTo(appointment.getId());
        assertThat(response.getPatientName()).isEqualTo("Joao");
    }

    @Test
    @DisplayName("agenda com plano de saude ativo")
    void shouldScheduleWithActivePlan() {
        stubActiveActors();
        com.vitalink.platform.entity.InsurancePlan plan = com.vitalink.platform.entity.InsurancePlan.builder()
                .name("Plano Ouro").status(RecordStatus.ACTIVE).build();
        UUID planId = UUID.randomUUID();
        plan.setId(planId);

        AppointmentRequest request = validRequest();
        request.setInsurancePlanId(planId);
        when(insurancePlanService.getEntityOrThrow(planId)).thenReturn(plan);
        when(appointmentRepository.findConflicts(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment a = invocation.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        AppointmentResponse response = service.schedule(request);

        assertThat(response.getInsurancePlanId()).isEqualTo(planId);
        assertThat(response.getInsurancePlanName()).isEqualTo("Plano Ouro");
    }

    @Test
    @DisplayName("falha ao reagendar quando ha conflito com outra consulta")
    void shouldFailRescheduleWithConflict() {
        Appointment appointment = buildScheduledAppointment(AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById(appointment.getId())).thenReturn(java.util.Optional.of(appointment));
        when(appointmentRepository.findConflictsExcluding(any(), any(), any(), any(), eq(appointment.getId())))
                .thenReturn(List.of(new Appointment()));

        AppointmentRescheduleRequest request = new AppointmentRescheduleRequest();
        request.setScheduledStart(NOW.plusDays(2));
        request.setScheduledEnd(NOW.plusDays(2).plusMinutes(30));

        assertThatThrownBy(() -> service.reschedule(appointment.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ja possui uma consulta");
    }

    @Test
    @DisplayName("lista consultas por paciente e por profissional")
    void shouldListByPatientAndProfessional() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        Appointment appointment = buildScheduledAppointment(AppointmentStatus.SCHEDULED);
        org.springframework.data.domain.Page<Appointment> page =
                new org.springframework.data.domain.PageImpl<>(List.of(appointment), pageable, 1);

        when(appointmentRepository.findByPatientId(patient.getId(), pageable)).thenReturn(page);
        when(appointmentRepository.findByProfessionalId(professional.getId(), pageable)).thenReturn(page);

        assertThat(service.listByPatient(patient.getId(), pageable).getTotalElements()).isEqualTo(1);
        assertThat(service.listByProfessional(professional.getId(), pageable).getTotalElements()).isEqualTo(1);
    }

    private Appointment buildScheduledAppointment(AppointmentStatus status) {
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .professional(professional)
                .organization(organization)
                .scheduledStart(NOW.plusDays(1))
                .scheduledEnd(NOW.plusDays(1).plusMinutes(30))
                .type(AppointmentType.IN_PERSON)
                .status(status)
                .build();
        appointment.setId(UUID.randomUUID());
        return appointment;
    }
}
