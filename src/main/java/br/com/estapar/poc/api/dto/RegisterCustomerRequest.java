package br.com.estapar.poc.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterCustomerRequest(
        @NotBlank(message = "O nome e obrigatorio")
        String name,

        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "Formato de e-mail invalido")
        String email,

        @NotBlank(message = "O CPF e obrigatorio")
        @Size(min = 11, max = 14, message = "O CPF deve ter entre 11 e 14 caracteres")
        String documentCpf,

        @NotBlank(message = "A senha e obrigatoria")
        @Size(min = 6, message = "A senha deve ter no minimo 6 caracteres")
        String password
) {
}
