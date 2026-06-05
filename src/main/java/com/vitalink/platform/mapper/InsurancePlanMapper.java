package com.vitalink.platform.mapper;

import com.vitalink.platform.dto.insurance.InsurancePlanResponse;
import com.vitalink.platform.entity.InsurancePlan;
import org.springframework.stereotype.Component;

@Component
public class InsurancePlanMapper {
    public InsurancePlanResponse toResponse(InsurancePlan entity) {
        return InsurancePlanResponse.builder()
                .id(entity.getId())
                .operatorId(entity.getOperator().getId())
                .operatorName(entity.getOperator().getLegalName())
                .name(entity.getName())
                .ansCode(entity.getAnsCode())
                .coverageType(entity.getCoverageType())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
