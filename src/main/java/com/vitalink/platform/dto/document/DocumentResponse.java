package com.vitalink.platform.dto.document;

import com.vitalink.platform.entity.enums.RecordStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class DocumentResponse {
    private UUID id;
    private UUID patientId;
    private UUID appointmentId;
    private String fileName;
    private String contentType;
    private Long sizeBytes;
    private String description;
    private RecordStatus status;
    private String downloadUrl;
    private Instant createdAt;
}
