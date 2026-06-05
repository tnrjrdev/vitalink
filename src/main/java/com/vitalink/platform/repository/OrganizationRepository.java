package com.vitalink.platform.repository;

import com.vitalink.platform.entity.Organization;
import com.vitalink.platform.entity.enums.OrganizationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    boolean existsByCnpj(String cnpj);

    Page<Organization> findByType(OrganizationType type, Pageable pageable);
}
