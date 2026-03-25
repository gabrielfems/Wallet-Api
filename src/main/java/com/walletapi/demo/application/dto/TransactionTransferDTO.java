package com.walletapi.demo.application.dto;

import com.walletapi.demo.domain.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionTransferDTO(

        @NotNull(message = "Valor obrigatório")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal amount,

        @NotNull(message = "Remetente obrigatório")
        Long senderId,

        @NotNull(message = "Destinatário obrigatório")
        Long receiverId
) {}
