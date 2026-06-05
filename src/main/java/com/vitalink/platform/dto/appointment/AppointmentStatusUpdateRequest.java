package com.vitalink.platform.dto.appointment;

import com.vitalink.platform.entity.enums.AppointmentStatus;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class AppointmentStatusUpdateRequest {
    @NotNull(message = "Novo status e obrigatorio")
    private AppointmentStatus status;

    @Size(max = 1000)
    private String notes;
}
