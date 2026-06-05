package com.vitalink.platform.mapper;

import com.vitalink.platform.common.domain.Address;
import com.vitalink.platform.dto.AddressDto;

public final class AddressMapper {
    private AddressMapper() {
    }

    public static Address toEntity(AddressDto dto) {
        if (dto == null) {
            return null;
        }
        return Address.builder()
                .street(dto.getStreet())
                .number(dto.getNumber())
                .complement(dto.getComplement())
                .district(dto.getDistrict())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .country(dto.getCountry())
                .build();
    }

    public static AddressDto toDto(Address entity) {
        if (entity == null) {
            return null;
        }
        return AddressDto.builder()
                .street(entity.getStreet())
                .number(entity.getNumber())
                .complement(entity.getComplement())
                .district(entity.getDistrict())
                .city(entity.getCity())
                .state(entity.getState())
                .zipCode(entity.getZipCode())
                .country(entity.getCountry())
                .build();
    }
}
