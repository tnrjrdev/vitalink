package com.vitalink.platform.dto.patient;

import com.vitalink.platform.dto.AddressDto;
import com.vitalink.platform.entity.enums.Gender;
import com.vitalink.platform.entity.enums.RecordStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class PatientResponse {
    private final UUID id;
    private final String fullName;
    private final String cpf;
    private final LocalDate birthDate;
    private final Gender gender;
    private final String email;
    private final String phone;
    private final RecordStatus status;
    private final AddressDto address;
    private final Instant createdAt;
    private final Instant updatedAt;
}
