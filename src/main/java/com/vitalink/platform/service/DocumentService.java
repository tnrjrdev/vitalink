package com.vitalink.platform.service;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.dto.document.DocumentResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.UUID;

public interface DocumentService {
    DocumentResponse upload(MultipartFile file, UUID patientId, UUID appointmentId, String description);

    DocumentResponse getById(UUID id);

    PageResponse<DocumentResponse> listByPatient(UUID patientId, Pageable pageable);

    URL getDownloadUrl(UUID id);

    void delete(UUID id);
}
