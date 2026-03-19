package com.walletapi.demo.application.exceptions;

public class AmountException extends RuntimeException {
    public AmountException() {super("Invalid transaction amount");}
}
