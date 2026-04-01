package com.walletapi.demo.application.exceptions;

public class GoalBoxNotFoundException extends RuntimeException {
    public GoalBoxNotFoundException(Long boxId) {
        super("Box with ID " + boxId + " not found");
    }
}