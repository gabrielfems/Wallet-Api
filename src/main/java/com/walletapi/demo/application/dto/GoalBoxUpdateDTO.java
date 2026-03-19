// GoalBoxUpdateDTO.java
package com.walletapi.demo.application.dto;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record GoalBoxUpdateDTO(

        String name,
        String description,

        @Positive(message = "Meta deve ser positiva")
        BigDecimal targetAmount

) {}