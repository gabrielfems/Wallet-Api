// GoalBoxCreateDTO.java
package com.walletapi.demo.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record GoalBoxCreateDTO(

        @NotBlank(message = "Nome é obrigatório")
        String name,

        String description,

        @NotNull(message = "Meta é obrigatória")
        @Positive(message = "Meta deve ser positiva")
        BigDecimal targetAmount

) {}