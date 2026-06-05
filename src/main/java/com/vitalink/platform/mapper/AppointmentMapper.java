package com.vitalink.platform.mapper;

import com.vitalink.platform.dto.appointment.AppointmentResponse;
import com.vitalink.platform.entity.Appointment;
import com.vitalink.platform.entity.InsurancePlan;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {
    public AppointmentResponse toResponse(Appointment entity) {
        InsurancePlan plan = entity.getInsurancePlan();
        return AppointmentResponse.builder()
                .id(entity.getId())
                .patientId(entity.getPatient().getId())
                .patientName(entity.getPatient().getFullName())
                .professionalId(entity.getProfessional().getId())
                .professionalName(entity.getProfessional().getFullName())
                .organizationId(entity.getOrganization().getId())
                .organizationName(entity.getOrganization().getLegalName())
                .insurancePlanId(plan != null ? plan.getId() : null)
                .insurancePlanName(plan != null ? plan.getName() : null)
                .scheduledStart(entity.getScheduledStart())
                .scheduledEnd(entity.getScheduledEnd())
                .status(entity.getStatus())
                .type(entity.getType())
                .reason(entity.getReason())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
