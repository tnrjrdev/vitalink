package com.vitalink.platform.service.impl;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.common.exception.BusinessException;
import com.vitalink.platform.common.exception.DuplicateResourceException;
import com.vitalink.platform.common.exception.ResourceNotFoundException;
import com.vitalink.platform.dto.insurance.InsurancePlanRequest;
import com.vitalink.platform.dto.insurance.InsurancePlanResponse;
import com.vitalink.platform.entity.InsurancePlan;
import com.vitalink.platform.entity.Organization;
import com.vitalink.platform.entity.enums.RecordStatus;
import com.vitalink.platform.mapper.InsurancePlanMapper;
import com.vitalink.platform.repository.InsurancePlanRepository;
import com.vitalink.platform.service.InsurancePlanService;
import com.vitalink.platform.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InsurancePlanServiceImpl implements InsurancePlanService {
    private final InsurancePlanRepository insurancePlanRepository;
    private final OrganizationService organizationService;
    private final InsurancePlanMapper insurancePlanMapper;

    @Override
    @Transactional
    public InsurancePlanResponse create(InsurancePlanRequest request) {
        if (insurancePlanRepository.existsByAnsCode(request.getAnsCode())) {
            throw new DuplicateResourceException("Plano", "codigo ANS", request.getAnsCode());
        }
        Organization operator = resolveOperator(request.getOperatorId());

        InsurancePlan entity = InsurancePlan.builder()
                .operator(operator)
                .name(request.getName())
                .ansCode(request.getAnsCode())
                .coverageType(request.getCoverageType())
                .status(RecordStatus.ACTIVE)
                .build();

        entity = insurancePlanRepository.save(entity);
        log.info("Plano de saude criado: id={}", entity.getId());
        return insurancePlanMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public InsurancePlanResponse update(UUID id, InsurancePlanRequest request) {
        InsurancePlan entity = getEntityOrThrow(id);

        if (!entity.getAnsCode().equals(request.getAnsCode())
                && insurancePlanRepository.existsByAnsCode(request.getAnsCode())) {
            throw new DuplicateResourceException("Plano", "codigo ANS", request.getAnsCode());
        }

        if (!entity.getOperator().getId().equals(request.getOperatorId())) {
            entity.setOperator(resolveOperator(request.getOperatorId()));
        }

        entity.setName(request.getName());
        entity.setAnsCode(request.getAnsCode());
        entity.setCoverageType(request.getCoverageType());

        log.info("Plano de saude atualizado: id={}", id);
        return insurancePlanMapper.toResponse(entity);
    }

    @Override
    public InsurancePlanResponse getById(UUID id) {
        return insurancePlanMapper.toResponse(getEntityOrThrow(id));
    }

    @Override
    public PageResponse<InsurancePlanResponse> list(UUID operatorId, Pageable pageable) {
        var page = (operatorId == null)
                ? insurancePlanRepository.findAll(pageable)
                : insurancePlanRepository.findByOperatorId(operatorId, pageable);
        return PageResponse.from(page.map(insurancePlanMapper::toResponse));
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        InsurancePlan entity = getEntityOrThrow(id);
        entity.setStatus(RecordStatus.INACTIVE);
        log.info("Plano de saude inativado: id={}", id);
    }

    @Override
    public InsurancePlan getEntityOrThrow(UUID id) {
        return insurancePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano de saude", "id", id));
    }

    private Organization resolveOperator(UUID operatorId) {
        Organization operator = organizationService.getEntityOrThrow(operatorId);
        if (!operator.isInsurer()) {
            throw new BusinessException(
                    "A organizacao informada nao e uma Operadora (INSURER) e nao pode ofertar planos");
        }
        return operator;
    }
}
