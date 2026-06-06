package com.vitalink.platform.mapper;

import com.vitalink.platform.dto.document.DocumentResponse;
import com.vitalink.platform.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {
    public DocumentResponse toResponse(Document entity, String downloadUrl) {
        return DocumentResponse.builder()
                .id(entity.getId())
                .patientId(entity.getPatientId())
                .appointmentId(entity.getAppointmentId())
                .fileName(entity.getFileName())
                .contentType(entity.getContentType())
                .sizeBytes(entity.getSizeBytes())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .downloadUrl(downloadUrl)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
