package com.walletapi.demo.application.exceptions;

public class GoalBoxNotFoundException extends RuntimeException {
    public GoalBoxNotFoundException(Long boxId) {
        super("Caixinha com ID " + boxId + " não encontrada");
    }
}