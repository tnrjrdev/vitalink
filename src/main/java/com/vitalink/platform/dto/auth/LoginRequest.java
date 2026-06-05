package com.vitalink.platform.dto.auth;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "E-mail e obrigatorio")
    @Email(message = "E-mail invalido")
    private String email;

    @NotBlank(message = "Senha e obrigatoria")
    private String password;
}
