package com.walletapi.demo.application.dto;

import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.enums.WalletStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        String document,
        String phone,
        String address,
        LocalDate birthDate,
        BigDecimal walletBalance,
        WalletStatus walletStatus
) {
    public static UserResponseDTO from(User u) {
        return new UserResponseDTO(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getDocument(),
                u.getPhone(),
                u.getCep(),
                u.getBirthDate(),
                u.getWallet().getBalance(),
                u.getWallet().getStatus()
        );
    }
}