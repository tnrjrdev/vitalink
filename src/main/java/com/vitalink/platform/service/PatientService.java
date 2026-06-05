package com.vitalink.platform.service;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.dto.patient.PatientRequest;
import com.vitalink.platform.dto.patient.PatientResponse;
import com.vitalink.platform.entity.Patient;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PatientService {
    PatientResponse create(PatientRequest request);

    PatientResponse update(UUID id, PatientRequest request);

    PatientResponse getById(UUID id);

    PageResponse<PatientResponse> list(Pageable pageable);

    void deactivate(UUID id);

    Patient getEntityOrThrow(UUID id);
}
