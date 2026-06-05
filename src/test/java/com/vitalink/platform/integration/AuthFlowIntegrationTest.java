package com.vitalink.platform.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitalink.platform.dto.auth.LoginRequest;
import com.vitalink.platform.dto.auth.RegisterRequest;
import com.vitalink.platform.dto.organization.OrganizationRequest;
import com.vitalink.platform.entity.enums.OrganizationType;
import com.vitalink.platform.entity.enums.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Fluxo de autenticacao e autorizacao (integracao)")
class AuthFlowIntegrationTest extends AbstractIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("registra paciente e retorna 201 com tokens")
    void shouldRegisterPatient() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Paciente Integrado");
        request.setEmail("paciente.int@medico.com");
        request.setPassword("Senha123");
        request.setRoles(Set.of(RoleName.PATIENT));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value("paciente.int@medico.com"));
    }

    @Test
    @DisplayName("rejeita registro com perfil ADMIN (422)")
    void shouldRejectAdminSelfRegister() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Hacker");
        request.setEmail("hacker.int@medico.com");
        request.setPassword("Senha123");
        request.setRoles(Set.of(RoleName.ADMIN));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("rejeita acesso a endpoint protegido sem token (401)")
    void shouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("ADMIN cria organizacao (201) e PACIENTE recebe 403")
    void shouldEnforceRbacOnOrganizationCreation() throws Exception {
        String adminToken = loginAndGetToken("admin@medico.com", "ChangeMe@123");

        OrganizationRequest orgRequest = new OrganizationRequest();
        orgRequest.setLegalName("Hospital Integrado LTDA");
        orgRequest.setCnpj("11222333000181");
        orgRequest.setType(OrganizationType.HOSPITAL);

        mockMvc.perform(post("/api/v1/organizations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orgRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cnpj").value("11222333000181"));

        String patientToken = registerAndGetToken("rbac.patient@medico.com");
        OrganizationRequest another = new OrganizationRequest();
        another.setLegalName("Tentativa Indevida");
        another.setCnpj("55666777000122");
        another.setType(OrganizationType.CLINIC);

        mockMvc.perform(post("/api/v1/organizations")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(another)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("valida corpo invalido no registro (400)")
    void shouldReturnBadRequestOnInvalidBody() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("");
        request.setEmail("invalido");
        request.setPassword("123");
        request.setRoles(Set.of(RoleName.PATIENT));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return extractAccessToken(result);
    }

    private String registerAndGetToken(String email) throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Paciente RBAC");
        request.setEmail(email);
        request.setPassword("Senha123");
        request.setRoles(Set.of(RoleName.PATIENT));

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return extractAccessToken(result);
    }

    private String extractAccessToken(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = node.get("accessToken").asText();
        assertThat(token).isNotBlank();
        return token;
    }
}
