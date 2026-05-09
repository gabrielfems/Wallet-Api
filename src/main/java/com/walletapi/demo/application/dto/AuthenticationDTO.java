package com.walletapi.demo.application.dto;

public record AuthenticationDTO(
        String login,
        String password
) {
}
