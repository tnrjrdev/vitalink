package com.vitalink.platform.mapper;

import com.vitalink.platform.dto.organization.OrganizationRequest;
import com.vitalink.platform.dto.organization.OrganizationResponse;
import com.vitalink.platform.entity.Organization;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {
    public Organization toEntity(OrganizationRequest request) {
        return Organization.builder()
                .legalName(request.getLegalName())
                .tradeName(request.getTradeName())
                .cnpj(request.getCnpj())
                .type(request.getType())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(AddressMapper.toEntity(request.getAddress()))
                .build();
    }

    public void updateEntity(Organization entity, OrganizationRequest request) {
        entity.setLegalName(request.getLegalName());
        entity.setTradeName(request.getTradeName());
        entity.setCnpj(request.getCnpj());
        entity.setType(request.getType());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setAddress(AddressMapper.toEntity(request.getAddress()));
    }

    public OrganizationResponse toResponse(Organization entity) {
        return OrganizationResponse.builder()
                .id(entity.getId())
                .legalName(entity.getLegalName())
                .tradeName(entity.getTradeName())
                .cnpj(entity.getCnpj())
                .type(entity.getType())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .status(entity.getStatus())
                .address(AddressMapper.toDto(entity.getAddress()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
