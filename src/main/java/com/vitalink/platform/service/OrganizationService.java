package com.vitalink.platform.service;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.dto.organization.OrganizationRequest;
import com.vitalink.platform.dto.organization.OrganizationResponse;
import com.vitalink.platform.entity.Organization;
import com.vitalink.platform.entity.enums.OrganizationType;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrganizationService {
    OrganizationResponse create(OrganizationRequest request);

    OrganizationResponse update(UUID id, OrganizationRequest request);

    OrganizationResponse getById(UUID id);

    PageResponse<OrganizationResponse> list(OrganizationType type, Pageable pageable);

    void deactivate(UUID id);

    OrganizationResponse activate(UUID id);

    Organization getEntityOrThrow(UUID id);
}
