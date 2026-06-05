package com.vitalink.platform.dto.appointment;

import com.vitalink.platform.entity.enums.AppointmentType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class AppointmentRequest {
    @NotNull(message = "Paciente e obrigatorio")
    private UUID patientId;

    @NotNull(message = "Profissional e obrigatorio")
    private UUID professionalId;

    @NotNull(message = "Organizacao (local) e obrigatoria")
    private UUID organizationId;

    private UUID insurancePlanId;

    @NotNull(message = "Inicio do agendamento e obrigatorio")
    @Future(message = "O agendamento deve ser no futuro")
    private OffsetDateTime scheduledStart;

    @NotNull(message = "Fim do agendamento e obrigatorio")
    @Future(message = "O agendamento deve ser no futuro")
    private OffsetDateTime scheduledEnd;

    @NotNull(message = "Tipo do atendimento e obrigatorio")
    private AppointmentType type;

    @Size(max = 500)
    private String reason;
}
