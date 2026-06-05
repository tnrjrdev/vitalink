package com.vitalink.platform.mapper;

import com.vitalink.platform.dto.professional.ProfessionalResponse;
import com.vitalink.platform.entity.HealthcareProfessional;
import org.springframework.stereotype.Component;

@Component
public class ProfessionalMapper {
    public ProfessionalResponse toResponse(HealthcareProfessional entity) {
        return ProfessionalResponse.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganization().getId())
                .organizationName(entity.getOrganization().getLegalName())
                .fullName(entity.getFullName())
                .cpf(entity.getCpf())
                .councilType(entity.getCouncilType())
                .councilNumber(entity.getCouncilNumber())
                .councilState(entity.getCouncilState())
                .specialty(entity.getSpecialty())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
