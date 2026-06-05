package com.vitalink.platform.dto.insurance;

import com.vitalink.platform.entity.enums.CoverageType;
import com.vitalink.platform.entity.enums.RecordStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class InsurancePlanResponse {
    private final UUID id;
    private final UUID operatorId;
    private final String operatorName;
    private final String name;
    private final String ansCode;
    private final CoverageType coverageType;
    private final RecordStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;
}
