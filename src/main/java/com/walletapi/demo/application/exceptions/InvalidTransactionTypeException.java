package com.walletapi.demo.application.exceptions;

public class InvalidTransactionTypeException extends RuntimeException {
    public InvalidTransactionTypeException() {
        super("Invalid transaction type");
    }
}