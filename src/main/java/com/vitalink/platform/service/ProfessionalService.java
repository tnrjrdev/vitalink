package com.vitalink.platform.service;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.dto.professional.ProfessionalRequest;
import com.vitalink.platform.dto.professional.ProfessionalResponse;
import com.vitalink.platform.entity.HealthcareProfessional;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProfessionalService {
    ProfessionalResponse create(ProfessionalRequest request);

    ProfessionalResponse update(UUID id, ProfessionalRequest request);

    ProfessionalResponse getById(UUID id);

    PageResponse<ProfessionalResponse> list(UUID organizationId, Pageable pageable);

    void deactivate(UUID id);

    HealthcareProfessional getEntityOrThrow(UUID id);
}
