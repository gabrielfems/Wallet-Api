package com.walletapi.demo.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.walletapi.demo.domain.entities.Transaction;
import com.walletapi.demo.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionResponseDTO(
        LocalDateTime date,
        TransactionType type,
        BigDecimal amount,
        String senderName,
        String receiverName
) {
    public static TransactionResponseDTO from(Transaction t) {
        return new TransactionResponseDTO(
                t.getCreatedAt(),
                t.getType(),
                t.getAmount(),
                t.getSenderName(),
                t.getReceiverName()
        );
    }
}
