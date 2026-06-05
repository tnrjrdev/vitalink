package com.vitalink.platform.repository;

import com.vitalink.platform.entity.HealthcareProfessional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HealthcareProfessionalRepository extends JpaRepository<HealthcareProfessional, UUID> {
    boolean existsByCpf(String cpf);

    Page<HealthcareProfessional> findByOrganizationId(UUID organizationId, Pageable pageable);
}
