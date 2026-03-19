package com.walletapi.demo.application.exceptions;

public class ReceiverUserNotFoundException extends RuntimeException {
    public ReceiverUserNotFoundException() {super("Destination User Not Found");}
}
