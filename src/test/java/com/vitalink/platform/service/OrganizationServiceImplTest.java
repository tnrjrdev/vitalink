package com.vitalink.platform.service;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.common.exception.DuplicateResourceException;
import com.vitalink.platform.common.exception.ResourceNotFoundException;
import com.vitalink.platform.dto.organization.OrganizationRequest;
import com.vitalink.platform.dto.organization.OrganizationResponse;
import com.vitalink.platform.entity.Organization;
import com.vitalink.platform.entity.enums.OrganizationType;
import com.vitalink.platform.entity.enums.RecordStatus;
import com.vitalink.platform.mapper.OrganizationMapper;
import com.vitalink.platform.repository.OrganizationRepository;
import com.vitalink.platform.service.impl.OrganizationServiceImpl;
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
@DisplayName("OrganizationServiceImpl")
class OrganizationServiceImplTest {
    @Mock private OrganizationRepository organizationRepository;

    private OrganizationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrganizationServiceImpl(organizationRepository, new OrganizationMapper());
    }

    private OrganizationRequest validRequest() {
        OrganizationRequest request = new OrganizationRequest();
        request.setLegalName("Hospital Central LTDA");
        request.setTradeName("Hospital Central");
        request.setCnpj("12345678000190");
        request.setType(OrganizationType.HOSPITAL);
        request.setEmail("contato@hc.com");
        return request;
    }

    @Test
    @DisplayName("cria organizacao")
    void shouldCreate() {
        when(organizationRepository.existsByCnpj("12345678000190")).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> {
            Organization o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        OrganizationResponse response = service.create(validRequest());

        assertThat(response.getCnpj()).isEqualTo("12345678000190");
        assertThat(response.getType()).isEqualTo(OrganizationType.HOSPITAL);
    }

    @Test
    @DisplayName("falha ao criar com CNPJ duplicado")
    void shouldFailDuplicateCnpj() {
        when(organizationRepository.existsByCnpj("12345678000190")).thenReturn(true);

        assertThatThrownBy(() -> service.create(validRequest()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(organizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualiza organizacao existente")
    void shouldUpdate() {
        Organization existing = Organization.builder()
                .legalName("Antigo").cnpj("12345678000190").type(OrganizationType.HOSPITAL)
                .status(RecordStatus.ACTIVE).build();
        UUID id = UUID.randomUUID();
        existing.setId(id);
        when(organizationRepository.findById(id)).thenReturn(Optional.of(existing));

        OrganizationResponse response = service.update(id, validRequest());

        assertThat(response.getLegalName()).isEqualTo("Hospital Central LTDA");
    }

    @Test
    @DisplayName("getById lanca 404")
    void shouldThrowNotFound() {
        UUID id = UUID.randomUUID();
        when(organizationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("lista por tipo")
    void shouldListByType() {
        Pageable pageable = PageRequest.of(0, 10);
        Organization org = Organization.builder()
                .legalName("Op").cnpj("99").type(OrganizationType.INSURER)
                .status(RecordStatus.ACTIVE).build();
        org.setId(UUID.randomUUID());
        Page<Organization> page = new PageImpl<>(List.of(org), pageable, 1);
        when(organizationRepository.findByType(OrganizationType.INSURER, pageable)).thenReturn(page);

        PageResponse<OrganizationResponse> response = service.list(OrganizationType.INSURER, pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("inativa organizacao (soft-delete)")
    void shouldDeactivate() {
        Organization existing = Organization.builder()
                .legalName("Op").cnpj("99").type(OrganizationType.HOSPITAL)
                .status(RecordStatus.ACTIVE).build();
        UUID id = UUID.randomUUID();
        existing.setId(id);
        when(organizationRepository.findById(id)).thenReturn(Optional.of(existing));

        service.deactivate(id);

        assertThat(existing.getStatus()).isEqualTo(RecordStatus.INACTIVE);
    }

    @Test
    @DisplayName("reativa organizacao inativada")
    void shouldActivate() {
        Organization existing = Organization.builder()
                .legalName("Op").cnpj("99").type(OrganizationType.HOSPITAL)
                .status(RecordStatus.INACTIVE).build();
        UUID id = UUID.randomUUID();
        existing.setId(id);
        when(organizationRepository.findById(id)).thenReturn(Optional.of(existing));

        OrganizationResponse response = service.activate(id);

        assertThat(existing.getStatus()).isEqualTo(RecordStatus.ACTIVE);
        assertThat(response.getStatus()).isEqualTo(RecordStatus.ACTIVE);
    }
}
