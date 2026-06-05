package com.vitalink.platform.service;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.dto.appointment.AppointmentRequest;
import com.vitalink.platform.dto.appointment.AppointmentRescheduleRequest;
import com.vitalink.platform.dto.appointment.AppointmentResponse;
import com.vitalink.platform.dto.appointment.AppointmentStatusUpdateRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AppointmentService {
    AppointmentResponse schedule(AppointmentRequest request);

    AppointmentResponse reschedule(UUID id, AppointmentRescheduleRequest request);

    AppointmentResponse updateStatus(UUID id, AppointmentStatusUpdateRequest request);

    AppointmentResponse getById(UUID id);

    PageResponse<AppointmentResponse> listByPatient(UUID patientId, Pageable pageable);

    PageResponse<AppointmentResponse> listByProfessional(UUID professionalId, Pageable pageable);
}
