package com.vitalink.platform.dto.appointment;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Getter
@Setter
public class AppointmentRescheduleRequest {
    @NotNull(message = "Novo inicio e obrigatorio")
    @Future(message = "O novo horario deve ser no futuro")
    private OffsetDateTime scheduledStart;

    @NotNull(message = "Novo fim e obrigatorio")
    @Future(message = "O novo horario deve ser no futuro")
    private OffsetDateTime scheduledEnd;
}
