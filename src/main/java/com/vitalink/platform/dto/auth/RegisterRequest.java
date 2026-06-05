package com.vitalink.platform.dto.auth;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "Nome completo e obrigatorio")
    @Size(max = 150)
    private String fullName;

    @NotBlank(message = "E-mail e obrigatorio")
    @Email(message = "E-mail invalido")
    @Size(max = 180)
    private String email;

    @NotBlank(message = "Senha e obrigatoria")
    @Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Senha deve conter ao menos uma letra minuscula, uma maiuscula e um numero")
    private String password;

    @NotEmpty(message = "Informe ao menos um perfil")
    private Set<String> roles;
}
