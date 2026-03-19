package com.walletapi.demo.application.exceptions;

public class UnauthorizedBoxAccessException extends RuntimeException {
    public UnauthorizedBoxAccessException() {super("This goal box does not belong to the user");}
}