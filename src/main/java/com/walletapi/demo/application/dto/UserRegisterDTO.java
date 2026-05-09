package com.walletapi.demo.application.dto;

import com.walletapi.demo.domain.enums.UserRole;

public record UserRegisterDTO(String login, String password, UserRole role) {
}
