package com.vitalink.platform.dto.patient;

import com.vitalink.platform.dto.AddressDto;
import com.vitalink.platform.entity.enums.Gender;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Getter
@Setter
public class PatientRequest {
    @NotBlank(message = "Nome completo e obrigatorio")
    @Size(max = 150)
    private String fullName;

    @NotBlank(message = "CPF e obrigatorio")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 digitos (somente numeros)")
    private String cpf;

    @NotNull(message = "Data de nascimento e obrigatoria")
    @Past(message = "Data de nascimento deve estar no passado")
    private LocalDate birthDate;

    private Gender gender;

    @Email(message = "E-mail invalido")
    @Size(max = 180)
    private String email;

    @Size(max = 20)
    private String phone;

    @Valid
    private AddressDto address;
}
