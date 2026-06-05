package com.vitalink.platform.service.impl;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.common.exception.DuplicateResourceException;
import com.vitalink.platform.common.exception.ResourceNotFoundException;
import com.vitalink.platform.dto.patient.PatientRequest;
import com.vitalink.platform.dto.patient.PatientResponse;
import com.vitalink.platform.entity.Patient;
import com.vitalink.platform.entity.enums.RecordStatus;
import com.vitalink.platform.mapper.PatientMapper;
import com.vitalink.platform.repository.PatientRepository;
import com.vitalink.platform.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    @Override
    @Transactional
    public PatientResponse create(PatientRequest request) {
        if (patientRepository.existsByCpf(request.getCpf())) {
            throw new DuplicateResourceException("Paciente", "CPF", request.getCpf());
        }
        Patient entity = patientMapper.toEntity(request);
        entity = patientRepository.save(entity);
        log.info("Paciente criado: id={}", entity.getId());
        return patientMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public PatientResponse update(UUID id, PatientRequest request) {
        Patient entity = getEntityOrThrow(id);

        if (!entity.getCpf().equals(request.getCpf())
                && patientRepository.existsByCpf(request.getCpf())) {
            throw new DuplicateResourceException("Paciente", "CPF", request.getCpf());
        }

        patientMapper.updateEntity(entity, request);
        log.info("Paciente atualizado: id={}", id);
        return patientMapper.toResponse(entity);
    }

    @Override
    public PatientResponse getById(UUID id) {
        return patientMapper.toResponse(getEntityOrThrow(id));
    }

    @Override
    public PageResponse<PatientResponse> list(Pageable pageable) {
        return PageResponse.from(patientRepository.findAll(pageable).map(patientMapper::toResponse));
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        Patient entity = getEntityOrThrow(id);
        entity.setStatus(RecordStatus.INACTIVE);
        log.info("Paciente inativado: id={}", id);
    }

    @Override
    @Transactional
    public PatientResponse activate(UUID id) {
        Patient entity = getEntityOrThrow(id);
        entity.setStatus(RecordStatus.ACTIVE);
        log.info("Paciente reativado: id={}", id);
        return patientMapper.toResponse(entity);
    }

    @Override
    public Patient getEntityOrThrow(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", "id", id));
    }
}
