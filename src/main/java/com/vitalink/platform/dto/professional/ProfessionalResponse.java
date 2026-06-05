package com.vitalink.platform.dto.professional;

import com.vitalink.platform.entity.enums.CouncilType;
import com.vitalink.platform.entity.enums.RecordStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ProfessionalResponse {
    private final UUID id;
    private final UUID organizationId;
    private final String organizationName;
    private final String fullName;
    private final String cpf;
    private final CouncilType councilType;
    private final String councilNumber;
    private final String councilState;
    private final String specialty;
    private final String email;
    private final String phone;
    private final RecordStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;
}
