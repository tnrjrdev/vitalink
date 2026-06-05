package com.medico.platform.service;

import com.medico.platform.common.dto.PageResponse;
import com.medico.platform.dto.organization.OrganizationRequest;
import com.medico.platform.dto.organization.OrganizationResponse;
import com.medico.platform.entity.Organization;
import com.medico.platform.entity.enums.OrganizationType;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrganizationService {

    OrganizationResponse create(OrganizationRequest request);

    OrganizationResponse update(UUID id, OrganizationRequest request);

    OrganizationResponse getById(UUID id);

    PageResponse<OrganizationResponse> list(OrganizationType type, Pageable pageable);

    /** inativa a organizacao (soft-delete). */
    void deactivate(UUID id);

    /** acesso interno para outros services (retorna a entidade gerenciada). */
    Organization getEntityOrThrow(UUID id);
}
