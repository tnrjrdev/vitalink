package com.vitalink.platform.dto.professional;

import com.vitalink.platform.entity.enums.CouncilType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.UUID;

@Getter
@Setter
public class ProfessionalRequest {
    @NotNull(message = "Organizacao e obrigatoria")
    private UUID organizationId;

    @NotBlank(message = "Nome completo e obrigatorio")
    @Size(max = 150)
    private String fullName;

    @NotBlank(message = "CPF e obrigatorio")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 digitos (somente numeros)")
    private String cpf;

    @NotNull(message = "Tipo de conselho e obrigatorio")
    private CouncilType councilType;

    @NotBlank(message = "Numero do conselho e obrigatorio")
    @Size(max = 20)
    private String councilNumber;

    @NotBlank(message = "UF do conselho e obrigatoria")
    @Size(min = 2, max = 2, message = "UF deve ter 2 caracteres")
    private String councilState;

    @Size(max = 120)
    private String specialty;

    @Email(message = "E-mail invalido")
    @Size(max = 180)
    private String email;

    @Size(max = 20)
    private String phone;
}
