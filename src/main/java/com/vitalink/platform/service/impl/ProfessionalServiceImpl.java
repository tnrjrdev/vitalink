package com.vitalink.platform.service.impl;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.common.exception.DuplicateResourceException;
import com.vitalink.platform.common.exception.ResourceNotFoundException;
import com.vitalink.platform.dto.professional.ProfessionalRequest;
import com.vitalink.platform.dto.professional.ProfessionalResponse;
import com.vitalink.platform.entity.HealthcareProfessional;
import com.vitalink.platform.entity.Organization;
import com.vitalink.platform.entity.enums.RecordStatus;
import com.vitalink.platform.mapper.ProfessionalMapper;
import com.vitalink.platform.repository.HealthcareProfessionalRepository;
import com.vitalink.platform.service.OrganizationService;
import com.vitalink.platform.service.ProfessionalService;
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
public class ProfessionalServiceImpl implements ProfessionalService {
    private final HealthcareProfessionalRepository professionalRepository;
    private final OrganizationService organizationService;
    private final ProfessionalMapper professionalMapper;

    @Override
    @Transactional
    public ProfessionalResponse create(ProfessionalRequest request) {
        if (professionalRepository.existsByCpf(request.getCpf())) {
            throw new DuplicateResourceException("Profissional", "CPF", request.getCpf());
        }
        Organization organization = organizationService.getEntityOrThrow(request.getOrganizationId());

        HealthcareProfessional entity = HealthcareProfessional.builder()
                .organization(organization)
                .fullName(request.getFullName())
                .cpf(request.getCpf())
                .councilType(request.getCouncilType())
                .councilNumber(request.getCouncilNumber())
                .councilState(request.getCouncilState().toUpperCase())
                .specialty(request.getSpecialty())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(RecordStatus.ACTIVE)
                .build();

        entity = professionalRepository.save(entity);
        log.info("Profissional criado: id={}", entity.getId());
        return professionalMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ProfessionalResponse update(UUID id, ProfessionalRequest request) {
        HealthcareProfessional entity = getEntityOrThrow(id);

        if (!entity.getCpf().equals(request.getCpf())
                && professionalRepository.existsByCpf(request.getCpf())) {
            throw new DuplicateResourceException("Profissional", "CPF", request.getCpf());
        }

        if (!entity.getOrganization().getId().equals(request.getOrganizationId())) {
            entity.setOrganization(organizationService.getEntityOrThrow(request.getOrganizationId()));
        }

        entity.setFullName(request.getFullName());
        entity.setCpf(request.getCpf());
        entity.setCouncilType(request.getCouncilType());
        entity.setCouncilNumber(request.getCouncilNumber());
        entity.setCouncilState(request.getCouncilState().toUpperCase());
        entity.setSpecialty(request.getSpecialty());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());

        log.info("Profissional atualizado: id={}", id);
        return professionalMapper.toResponse(entity);
    }

    @Override
    public ProfessionalResponse getById(UUID id) {
        return professionalMapper.toResponse(getEntityOrThrow(id));
    }

    @Override
    public PageResponse<ProfessionalResponse> list(UUID organizationId, Pageable pageable) {
        var page = (organizationId == null)
                ? professionalRepository.findAll(pageable)
                : professionalRepository.findByOrganizationId(organizationId, pageable);
        return PageResponse.from(page.map(professionalMapper::toResponse));
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        HealthcareProfessional entity = getEntityOrThrow(id);
        entity.setStatus(RecordStatus.INACTIVE);
        log.info("Profissional inativado: id={}", id);
    }

    @Override
    public HealthcareProfessional getEntityOrThrow(UUID id) {
        return professionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional", "id", id));
    }
}
