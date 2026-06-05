package com.vitalink.platform.service;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.dto.insurance.InsurancePlanRequest;
import com.vitalink.platform.dto.insurance.InsurancePlanResponse;
import com.vitalink.platform.entity.InsurancePlan;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InsurancePlanService {
    InsurancePlanResponse create(InsurancePlanRequest request);

    InsurancePlanResponse update(UUID id, InsurancePlanRequest request);

    InsurancePlanResponse getById(UUID id);

    PageResponse<InsurancePlanResponse> list(UUID operatorId, Pageable pageable);

    void deactivate(UUID id);

    InsurancePlan getEntityOrThrow(UUID id);
}
