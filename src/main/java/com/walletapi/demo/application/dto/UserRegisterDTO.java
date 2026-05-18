package com.walletapi.demo.application.dto;

import com.walletapi.demo.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UserRegisterDTO(

        @Email(message = "E-mail inválido")
        String login,

        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%!]).{8,}$",
                message = "Senha fraca"
        )
        String password,

        UserRole role) {
}
