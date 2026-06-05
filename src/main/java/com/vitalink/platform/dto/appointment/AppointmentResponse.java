package com.vitalink.platform.dto.appointment;

import com.vitalink.platform.entity.enums.AppointmentStatus;
import com.vitalink.platform.entity.enums.AppointmentType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class AppointmentResponse {
    private final UUID id;

    private final UUID patientId;
    private final String patientName;

    private final UUID professionalId;
    private final String professionalName;

    private final UUID organizationId;
    private final String organizationName;

    private final UUID insurancePlanId;
    private final String insurancePlanName;

    private final OffsetDateTime scheduledStart;
    private final OffsetDateTime scheduledEnd;
    private final AppointmentStatus status;
    private final AppointmentType type;
    private final String reason;
    private final String notes;

    private final Instant createdAt;
    private final Instant updatedAt;
}
