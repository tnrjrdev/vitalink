package com.vitalink.platform.repository;

import com.vitalink.platform.entity.InsurancePlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, UUID> {
    boolean existsByAnsCode(String ansCode);

    Page<InsurancePlan> findByOperatorId(UUID operatorId, Pageable pageable);
}
