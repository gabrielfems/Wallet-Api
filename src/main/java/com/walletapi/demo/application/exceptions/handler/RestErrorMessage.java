package com.walletapi.demo.application.exceptions.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
public class RestErrorMessage {
    private HttpStatus status;
    private String message;
}
