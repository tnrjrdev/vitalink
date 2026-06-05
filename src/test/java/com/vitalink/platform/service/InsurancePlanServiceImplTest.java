package com.vitalink.platform.service;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.common.exception.BusinessException;
import com.vitalink.platform.common.exception.DuplicateResourceException;
import com.vitalink.platform.common.exception.ResourceNotFoundException;
import com.vitalink.platform.dto.insurance.InsurancePlanRequest;
import com.vitalink.platform.dto.insurance.InsurancePlanResponse;
import com.vitalink.platform.entity.InsurancePlan;
import com.vitalink.platform.entity.Organization;
import com.vitalink.platform.entity.enums.CoverageType;
import com.vitalink.platform.entity.enums.OrganizationType;
import com.vitalink.platform.entity.enums.RecordStatus;
import com.vitalink.platform.mapper.InsurancePlanMapper;
import com.vitalink.platform.repository.InsurancePlanRepository;
import com.vitalink.platform.service.impl.InsurancePlanServiceImpl;
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
@DisplayName("InsurancePlanServiceImpl")
class InsurancePlanServiceImplTest {
    @Mock private InsurancePlanRepository insurancePlanRepository;
    @Mock private OrganizationService organizationService;

    private InsurancePlanServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InsurancePlanServiceImpl(insurancePlanRepository, organizationService, new InsurancePlanMapper());
    }

    private Organization org(OrganizationType type) {
        Organization o = Organization.builder()
                .legalName("Operadora Y").cnpj("1").type(type).status(RecordStatus.ACTIVE).build();
        o.setId(UUID.randomUUID());
        return o;
    }

    private InsurancePlanRequest validRequest(UUID operatorId) {
        InsurancePlanRequest request = new InsurancePlanRequest();
        request.setOperatorId(operatorId);
        request.setName("Plano Ouro");
        request.setAnsCode("ANS-001");
        request.setCoverageType(CoverageType.COMPLETO);
        return request;
    }

    private InsurancePlan existing(Organization operator) {
        InsurancePlan p = InsurancePlan.builder()
                .operator(operator).name("Plano Antigo").ansCode("ANS-001")
                .coverageType(CoverageType.AMBULATORIAL).status(RecordStatus.ACTIVE).build();
        p.setId(UUID.randomUUID());
        return p;
    }

    @Test
    @DisplayName("cria plano para operadora valida")
    void shouldCreate() {
        Organization operator = org(OrganizationType.INSURER);
        when(insurancePlanRepository.existsByAnsCode("ANS-001")).thenReturn(false);
        when(organizationService.getEntityOrThrow(operator.getId())).thenReturn(operator);
        when(insurancePlanRepository.save(any(InsurancePlan.class))).thenAnswer(inv -> {
            InsurancePlan p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        InsurancePlanResponse response = service.create(validRequest(operator.getId()));

        assertThat(response.getAnsCode()).isEqualTo("ANS-001");
        assertThat(response.getOperatorId()).isEqualTo(operator.getId());
    }

    @Test
    @DisplayName("falha quando a organizacao nao e operadora")
    void shouldFailWhenNotInsurer() {
        Organization hospital = org(OrganizationType.HOSPITAL);
        when(insurancePlanRepository.existsByAnsCode("ANS-001")).thenReturn(false);
        when(organizationService.getEntityOrThrow(hospital.getId())).thenReturn(hospital);

        assertThatThrownBy(() -> service.create(validRequest(hospital.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nao e uma Operadora");
        verify(insurancePlanRepository, never()).save(any());
    }

    @Test
    @DisplayName("falha com codigo ANS duplicado")
    void shouldFailDuplicateAns() {
        when(insurancePlanRepository.existsByAnsCode("ANS-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(validRequest(UUID.randomUUID())))
                .isInstanceOf(DuplicateResourceException.class);
        verify(insurancePlanRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualiza plano existente")
    void shouldUpdate() {
        Organization operator = org(OrganizationType.INSURER);
        InsurancePlan plan = existing(operator);
        InsurancePlanRequest request = validRequest(operator.getId());
        request.setName("Plano Premium");

        when(insurancePlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));

        InsurancePlanResponse response = service.update(plan.getId(), request);

        assertThat(response.getName()).isEqualTo("Plano Premium");
    }

    @Test
    @DisplayName("getById lanca 404")
    void shouldThrowNotFound() {
        UUID id = UUID.randomUUID();
        when(insurancePlanRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("lista todos sem filtro")
    void shouldListAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<InsurancePlan> page = new PageImpl<>(List.of(existing(org(OrganizationType.INSURER))), pageable, 1);
        when(insurancePlanRepository.findAll(pageable)).thenReturn(page);

        PageResponse<InsurancePlanResponse> response = service.list(null, pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("lista por operadora")
    void shouldListByOperator() {
        Organization operator = org(OrganizationType.INSURER);
        Pageable pageable = PageRequest.of(0, 10);
        Page<InsurancePlan> page = new PageImpl<>(List.of(existing(operator)), pageable, 1);
        when(insurancePlanRepository.findByOperatorId(operator.getId(), pageable)).thenReturn(page);

        PageResponse<InsurancePlanResponse> response = service.list(operator.getId(), pageable);

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("inativa plano")
    void shouldDeactivate() {
        InsurancePlan plan = existing(org(OrganizationType.INSURER));
        when(insurancePlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));

        service.deactivate(plan.getId());

        assertThat(plan.getStatus()).isEqualTo(RecordStatus.INACTIVE);
    }

    @Test
    @DisplayName("reativa plano inativado")
    void shouldActivate() {
        InsurancePlan plan = existing(org(OrganizationType.INSURER));
        plan.setStatus(RecordStatus.INACTIVE);
        when(insurancePlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));

        InsurancePlanResponse response = service.activate(plan.getId());

        assertThat(plan.getStatus()).isEqualTo(RecordStatus.ACTIVE);
        assertThat(response.getStatus()).isEqualTo(RecordStatus.ACTIVE);
    }
}
