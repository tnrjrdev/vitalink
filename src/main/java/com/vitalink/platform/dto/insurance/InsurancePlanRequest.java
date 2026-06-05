package com.vitalink.platform.dto.insurance;

import com.vitalink.platform.entity.enums.CoverageType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Getter
@Setter
public class InsurancePlanRequest {
    @NotNull(message = "Operadora e obrigatoria")
    private UUID operatorId;

    @NotBlank(message = "Nome do plano e obrigatorio")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Codigo ANS e obrigatorio")
    @Size(max = 20)
    private String ansCode;

    @NotNull(message = "Tipo de cobertura e obrigatorio")
    private CoverageType coverageType;
}
