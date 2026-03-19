package com.walletapi.demo.application.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserCreateDTO(

        @NotBlank(message = "Nome obrigatório")
        @Size(min = 3, max = 100)
        String name,

        @Email(message = "E-mail inválido")
        @NotBlank(message = "E-mail obrigatório")
        String email,

        @NotBlank(message = "Senha obrigatória")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%!])\\S{8,}$",
                message = "Senha fraca. A senha deve ter no mínimo 8 caracteres, letras maiúsculas, minúsculas, número e caractere especial (@#$%!)."
        )
        String password,

        @NotBlank(message = "Telefone obrigatório")
        @Pattern(
                regexp = "^\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4}$",
                message = "Telefone inválido. Ex: (11) 99999-9999"
        )
        String phone,

        @NotBlank(message = "CEP obrigatório")
        @Pattern(regexp = "\\d{8}|\\d{5}-\\d{3}", message = "CEP inválido. Formato: 12345678 ou 12345-678")
        String cep,

        @NotBlank(message = "Número obrigatório")
        @Size(min = 1, max = 5)
        String numero,

        String complemento,

        @NotNull(message = "Data de nascimento obrigatória")
        @Past(message = "Data de nascimento deve ser no passado")
        LocalDate birthDate,

        @Pattern(
                regexp = "^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2})$",
                message = "Documento inválido. Ex: CPF: 123.456.789-00 ou CNPJ: 12.345.678/0001-00"
        )
        @NotBlank(message = "Documento obrigatório")
        String document

) {}