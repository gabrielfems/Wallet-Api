package com.walletapi.demo.application.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserUpdateDTO(

        @Size(min = 3, max = 100)
        String name,

        @Email(message = "E-mail inválido")
        String email,

        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%!]).{8,}$",
                message = "Senha fraca"
        )
        String password,

        @Pattern(
                regexp = "^\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4}$",
                message = "Telefone inválido. Ex: (11) 99999-9999"
        )
        String phone,

        @Pattern(regexp = "\\d{8}|\\d{5}-\\d{3}", message = "CEP inválido. Formato: 12345678 ou 12345-678")
        String cep,

        @Past(message = "Data de nascimento deve ser no passado")
        LocalDate birthDate,

        @Pattern(
                regexp = "^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2})$",
                message = "Documento inválido. Ex: CPF: 123.456.789-00 ou CNPJ: 12.345.678/0001-00"
        )
        String document

) {}