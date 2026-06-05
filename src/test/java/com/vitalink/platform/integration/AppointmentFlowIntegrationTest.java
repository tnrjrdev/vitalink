package com.vitalink.platform.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitalink.platform.dto.appointment.AppointmentRequest;
import com.vitalink.platform.dto.appointment.AppointmentStatusUpdateRequest;
import com.vitalink.platform.dto.auth.LoginRequest;
import com.vitalink.platform.dto.patient.PatientRequest;
import com.vitalink.platform.dto.professional.ProfessionalRequest;
import com.vitalink.platform.dto.organization.OrganizationRequest;
import com.vitalink.platform.entity.enums.AppointmentStatus;
import com.vitalink.platform.entity.enums.AppointmentType;
import com.vitalink.platform.entity.enums.CouncilType;
import com.vitalink.platform.entity.enums.OrganizationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Fluxo completo de agendamento (integracao)")
class AppointmentFlowIntegrationTest extends AbstractIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("agenda consulta, confirma e bloqueia conflito de horario")
    void shouldScheduleConfirmAndPreventConflict() throws Exception {
        String token = loginAdmin();

        String organizationId = createOrganization(token, "33444555000166");
        String professionalId = createProfessional(token, organizationId, "11122233344");
        String patientId = createPatient(token, "55566677788");

        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC).plusDays(3).withNano(0);
        OffsetDateTime end = start.plusMinutes(30);

        AppointmentRequest appointment = new AppointmentRequest();
        appointment.setPatientId(java.util.UUID.fromString(patientId));
        appointment.setProfessionalId(java.util.UUID.fromString(professionalId));
        appointment.setOrganizationId(java.util.UUID.fromString(organizationId));
        appointment.setScheduledStart(start);
        appointment.setScheduledEnd(end);
        appointment.setType(AppointmentType.IN_PERSON);
        appointment.setReason("Check-up");

        MvcResult scheduled = mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(AppointmentStatus.SCHEDULED.name()))
                .andReturn();

        String appointmentId = objectMapper.readTree(scheduled.getResponse().getContentAsString())
                .get("id").asText();

        AppointmentStatusUpdateRequest statusUpdate = new AppointmentStatusUpdateRequest();
        statusUpdate.setStatus(AppointmentStatus.CONFIRMED);
        mockMvc.perform(patch("/api/v1/appointments/" + appointmentId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(AppointmentStatus.CONFIRMED.name()));

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointment)))
                .andExpect(status().isUnprocessableEntity());
    }

    private String loginAdmin() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("admin@medico.com");
        login.setPassword("ChangeMe@123");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        return readField(result, "accessToken");
    }

    private String createOrganization(String token, String cnpj) throws Exception {
        OrganizationRequest request = new OrganizationRequest();
        request.setLegalName("Clinica Flow LTDA");
        request.setCnpj(cnpj);
        request.setType(OrganizationType.CLINIC);
        MvcResult result = mockMvc.perform(post("/api/v1/organizations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return readField(result, "id");
    }

    private String createProfessional(String token, String organizationId, String cpf) throws Exception {
        ProfessionalRequest request = new ProfessionalRequest();
        request.setOrganizationId(java.util.UUID.fromString(organizationId));
        request.setFullName("Dra. Flow");
        request.setCpf(cpf);
        request.setCouncilType(CouncilType.CRM);
        request.setCouncilNumber("99999");
        request.setCouncilState("SP");
        request.setSpecialty("Clinica Geral");
        MvcResult result = mockMvc.perform(post("/api/v1/professionals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return readField(result, "id");
    }

    private String createPatient(String token, String cpf) throws Exception {
        PatientRequest request = new PatientRequest();
        request.setFullName("Paciente Flow");
        request.setCpf(cpf);
        request.setBirthDate(LocalDate.of(1985, 5, 20));
        MvcResult result = mockMvc.perform(post("/api/v1/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return readField(result, "id");
    }

    private String readField(MvcResult result, String field) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get(field).asText();
    }
}
