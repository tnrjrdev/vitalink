package com.vitalink.platform.service;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.common.exception.DuplicateResourceException;
import com.vitalink.platform.common.exception.ResourceNotFoundException;
import com.vitalink.platform.dto.professional.ProfessionalRequest;
import com.vitalink.platform.dto.professional.ProfessionalResponse;
import com.vitalink.platform.entity.HealthcareProfessional;
import com.vitalink.platform.entity.Organization;
import com.vitalink.platform.entity.enums.CouncilType;
import com.vitalink.platform.entity.enums.OrganizationType;
import com.vitalink.platform.entity.enums.RecordStatus;
import com.vitalink.platform.mapper.ProfessionalMapper;
import com.vitalink.platform.repository.HealthcareProfessionalRepository;
import com.vitalink.platform.service.impl.ProfessionalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfessionalServiceImpl")
class ProfessionalServiceImplTest {
    @Mock private HealthcareProfessionalRepository professionalRepository;
    @Mock private OrganizationService organizationService;

    private ProfessionalServiceImpl service;
    private Organization organization;

    @BeforeEach
    void setUp() {
        service = new ProfessionalServiceImpl(professionalRepository, organizationService, new ProfessionalMapper());
        organization = org(OrganizationType.HOSPITAL);
    }

    private Organization org(OrganizationType type) {
        Organization o = Organization.builder()
                .legalName("Hospital X").cnpj("1").type(type).status(RecordStatus.ACTIVE).build();
        o.setId(UUID.randomUUID());
        return o;
    }

    private ProfessionalRequest validRequest() {
        ProfessionalRequest request = new ProfessionalRequest();
        request.setOrganizationId(organization.getId());
        request.setFullName("Dr. Carlos");
        request.setCpf("98765432100");
        request.setCouncilType(CouncilType.CRM);
        request.setCouncilNumber("12345");
        request.setCouncilState("sp");
        request.setSpecialty("Cardiologia");
        return request;
    }

    private HealthcareProfessional existing() {
        HealthcareProfessional p = HealthcareProfessional.builder()
                .organization(organization).fullName("Dr. Antigo").cpf("98765432100")
                .councilType(CouncilType.CRM).councilNumber("12345").councilState("SP")
                .status(RecordStatus.ACTIVE).build();
        p.setId(UUID.randomUUID());
        return p;
    }

    @Test
    @DisplayName("cria profissional e normaliza UF do conselho")
    void shouldCreate() {
        when(professionalRepository.existsByCpf("98765432100")).thenReturn(false);
        when(organizationService.getEntityOrThrow(organization.getId())).thenReturn(organization);
        when(professionalRepository.save(any(HealthcareProfessional.class))).thenAnswer(inv -> {
            HealthcareProfessional p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        ProfessionalResponse response = service.create(validRequest());

        assertThat(response.getCouncilState()).isEqualTo("SP");
        assertThat(response.getOrganizationId()).isEqualTo(organization.getId());
    }

    @Test
    @DisplayName("falha ao criar com CPF duplicado")
    void shouldFailDuplicateCpf() {
        when(professionalRepository.existsByCpf("98765432100")).thenReturn(true);

        assertThatThrownBy(() -> service.create(validRequest()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(professionalRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualiza profissional alterando a organizacao")
    void shouldUpdateChangingOrganization() {
        HealthcareProfessional p = existing();
        Organization other = org(OrganizationType.CLINIC);
        ProfessionalRequest request = validRequest();
        request.setOrganizationId(other.getId());
        request.setFullName("Dr. Novo");

        when(professionalRepository.findById(p.getId())).thenReturn(Optional.of(p));
        when(organizationService.getEntityOrThrow(other.getId())).thenReturn(other);

        ProfessionalResponse response = service.update(p.getId(), request);

        assertThat(response.getFullName()).isEqualTo("Dr. Novo");
        assertThat(response.getOrganizationId()).isEqualTo(other.getId());
    }

    @Test
    @DisplayName("falha ao atualizar para um CPF ja usado por outro")
    void shouldFailUpdateDuplicateCpf() {
        HealthcareProfessional p = existing();
        ProfessionalRequest request = validRequest();
        request.setCpf("11111111111");

        when(professionalRepository.findById(p.getId())).thenReturn(Optional.of(p));
        when(professionalRepository.existsByCpf("11111111111")).thenReturn(true);

        assertThatThrownBy(() -> service.update(p.getId(), request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("getById lanca 404")
    void shouldThrowNotFound() {
        UUID id = UUID.randomUUID();
        when(professionalRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getById retorna o profissional")
    void shouldGetById() {
        HealthcareProfessional p = existing();
        when(professionalRepository.findById(p.getId())).thenReturn(Optional.of(p));

        assertThat(service.getById(p.getId()).getCpf()).isEqualTo("98765432100");
    }

    @Test
    @DisplayName("lista todos quando nao ha filtro de organizacao")
    void shouldListAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<HealthcareProfessional> page = new PageImpl<>(List.of(existing()), pageable, 1);
        when(professionalRepository.findAll(pageable)).thenReturn(page);

        PageResponse<ProfessionalResponse> response = service.list(null, pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("lista por organizacao quando filtrado")
    void shouldListByOrganization() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<HealthcareProfessional> page = new PageImpl<>(List.of(existing()), pageable, 1);
        when(professionalRepository.findByOrganizationId(organization.getId(), pageable)).thenReturn(page);

        PageResponse<ProfessionalResponse> response = service.list(organization.getId(), pageable);

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("inativa profissional")
    void shouldDeactivate() {
        HealthcareProfessional p = existing();
        when(professionalRepository.findById(p.getId())).thenReturn(Optional.of(p));

        service.deactivate(p.getId());

        assertThat(p.getStatus()).isEqualTo(RecordStatus.INACTIVE);
    }
}
