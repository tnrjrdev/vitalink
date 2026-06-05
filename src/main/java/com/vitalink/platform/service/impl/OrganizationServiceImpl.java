package com.vitalink.platform.service.impl;

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
public class OrganizationServiceImpl implements OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Override
    @Transactional
    public OrganizationResponse create(OrganizationRequest request) {
        if (organizationRepository.existsByCnpj(request.getCnpj())) {
            throw new DuplicateResourceException("Organizacao", "CNPJ", request.getCnpj());
        }
        Organization entity = organizationMapper.toEntity(request);
        entity = organizationRepository.save(entity);
        log.info("Organizacao criada: id={}, tipo={}", entity.getId(), entity.getType());
        return organizationMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public OrganizationResponse update(UUID id, OrganizationRequest request) {
        Organization entity = getEntityOrThrow(id);

        if (!entity.getCnpj().equals(request.getCnpj())
                && organizationRepository.existsByCnpj(request.getCnpj())) {
            throw new DuplicateResourceException("Organizacao", "CNPJ", request.getCnpj());
        }

        organizationMapper.updateEntity(entity, request);
        log.info("Organizacao atualizada: id={}", id);
        return organizationMapper.toResponse(entity);
    }

    @Override
    public OrganizationResponse getById(UUID id) {
        return organizationMapper.toResponse(getEntityOrThrow(id));
    }

    @Override
    public PageResponse<OrganizationResponse> list(OrganizationType type, Pageable pageable) {
        var page = (type == null)
                ? organizationRepository.findAll(pageable)
                : organizationRepository.findByType(type, pageable);
        return PageResponse.from(page.map(organizationMapper::toResponse));
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        Organization entity = getEntityOrThrow(id);
        entity.setStatus(RecordStatus.INACTIVE);
        log.info("Organizacao inativada: id={}", id);
    }

    @Override
    public Organization getEntityOrThrow(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organizacao", "id", id));
    }
}
