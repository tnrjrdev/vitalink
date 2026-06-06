package com.vitalink.platform.repository;

import com.vitalink.platform.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Page<Document> findByPatientId(UUID patientId, Pageable pageable);
}
