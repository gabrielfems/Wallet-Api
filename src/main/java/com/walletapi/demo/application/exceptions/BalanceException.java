package com.walletapi.demo.application.exceptions;

public class BalanceException extends RuntimeException {
  public BalanceException() {super("Insufficient balance");}
}
