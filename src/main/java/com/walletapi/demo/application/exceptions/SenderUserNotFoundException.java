package com.walletapi.demo.application.exceptions;


public class SenderUserNotFoundException extends RuntimeException {
    public SenderUserNotFoundException() { super("Sender User not found");}
}
