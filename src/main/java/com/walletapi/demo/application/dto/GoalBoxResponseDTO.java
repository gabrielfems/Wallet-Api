// GoalBoxResponseDTO.java
package com.walletapi.demo.application.dto;

import com.walletapi.demo.domain.entities.GoalBox;
import java.math.BigDecimal;
import java.math.RoundingMode;

public record GoalBoxResponseDTO(
        Long id,
        String name,
        String description,
        BigDecimal targetAmount,
        BigDecimal currentBalance,
        String progress,
        Long userId
) {
    public static GoalBoxResponseDTO from(GoalBox box) {
        String progress = "0%";
        if (box.getTargetAmount() != null && box.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentage = box.getCurrentBalance()
                    .multiply(new BigDecimal("100"))
                    .divide(box.getTargetAmount(), 0, RoundingMode.HALF_UP);
            progress = percentage + "%";
        }

        return new GoalBoxResponseDTO(
                box.getId(),
                box.getName(),
                box.getDescription(),
                box.getTargetAmount(),
                box.getCurrentBalance(),
                progress,
                box.getUser().getId()
        );
    }
}