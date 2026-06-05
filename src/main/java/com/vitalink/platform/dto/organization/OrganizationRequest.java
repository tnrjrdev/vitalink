package com.vitalink.platform.dto.organization;

import com.vitalink.platform.dto.AddressDto;
import com.vitalink.platform.entity.enums.OrganizationType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class OrganizationRequest {
    @NotBlank(message = "Razao social e obrigatoria")
    @Size(max = 180)
    private String legalName;

    @Size(max = 180)
    private String tradeName;

    @NotBlank(message = "CNPJ e obrigatorio")
    @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter 14 digitos (somente numeros)")
    private String cnpj;

    @NotNull(message = "Tipo da organizacao e obrigatorio")
    private OrganizationType type;

    @Email(message = "E-mail invalido")
    @Size(max = 180)
    private String email;

    @Size(max = 20)
    private String phone;

    @Valid
    private AddressDto address;
}
