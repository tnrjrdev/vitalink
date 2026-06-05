package com.vitalink.platform.mapper;

import com.vitalink.platform.dto.patient.PatientRequest;
import com.vitalink.platform.dto.patient.PatientResponse;
import com.vitalink.platform.entity.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {
    public Patient toEntity(PatientRequest request) {
        return Patient.builder()
                .fullName(request.getFullName())
                .cpf(request.getCpf())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(AddressMapper.toEntity(request.getAddress()))
                .build();
    }

    public void updateEntity(Patient entity, PatientRequest request) {
        entity.setFullName(request.getFullName());
        entity.setCpf(request.getCpf());
        entity.setBirthDate(request.getBirthDate());
        entity.setGender(request.getGender());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setAddress(AddressMapper.toEntity(request.getAddress()));
    }

    public PatientResponse toResponse(Patient entity) {
        return PatientResponse.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .cpf(entity.getCpf())
                .birthDate(entity.getBirthDate())
                .gender(entity.getGender())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .status(entity.getStatus())
                .address(AddressMapper.toDto(entity.getAddress()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
