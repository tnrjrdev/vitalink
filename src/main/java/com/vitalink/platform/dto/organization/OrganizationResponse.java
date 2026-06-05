package com.vitalink.platform.dto.organization;

import com.vitalink.platform.dto.AddressDto;
import com.vitalink.platform.entity.enums.OrganizationType;
import com.vitalink.platform.entity.enums.RecordStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class OrganizationResponse {
    private final UUID id;
    private final String legalName;
    private final String tradeName;
    private final String cnpj;
    private final OrganizationType type;
    private final String email;
    private final String phone;
    private final RecordStatus status;
    private final AddressDto address;
    private final Instant createdAt;
    private final Instant updatedAt;
}
