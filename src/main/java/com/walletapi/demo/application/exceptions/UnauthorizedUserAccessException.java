package com.walletapi.demo.application.exceptions;

public class UnauthorizedUserAccessException extends RuntimeException {
    public UnauthorizedUserAccessException() {
        super("Você não tem permissão para acessar esse recurso");
    }
}
