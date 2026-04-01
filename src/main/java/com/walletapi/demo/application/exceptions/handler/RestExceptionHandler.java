package com.walletapi.demo.application.exceptions.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.walletapi.demo.application.exceptions.*;
import io.micrometer.common.lang.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    private ResponseEntity<RestErrorMessage> userNotFoundHandler(UserNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(SenderUserNotFoundException.class)
    private ResponseEntity<RestErrorMessage> senderUserNotFoundHandler(SenderUserNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ReceiverUserNotFoundException.class)
    private ResponseEntity<RestErrorMessage> receiverNotFoundHandler(ReceiverUserNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(GoalBoxNotFoundException.class)
    private ResponseEntity<RestErrorMessage> goalBoxNotFoundHandler(GoalBoxNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(BalanceException.class)
    private ResponseEntity<RestErrorMessage> balanceHandler(BalanceException exception) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    private ResponseEntity<RestErrorMessage> insufficientBalanceHandler(InsufficientBalanceException exception) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
    }

    @ExceptionHandler(InvalidTransactionTypeException.class)
    private ResponseEntity<RestErrorMessage> invalidTransactionTypeHandler(InvalidTransactionTypeException exception) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
    }

    @ExceptionHandler(CepNotFoundException.class)
    private ResponseEntity<RestErrorMessage> cepNotFoundHandler(CepNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(UnauthorizedBoxAccessException.class)
    private ResponseEntity<RestErrorMessage> unauthorizedBoxAccessHandler(UnauthorizedBoxAccessException exception) {
        return buildResponse(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NonNull HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        String message = "Invalid Request.";
        if (ex.getCause() instanceof InvalidFormatException cause && cause.getTargetType().isEnum()) {
            message = "Invalid Transaction Type: " + cause.getValue();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RestErrorMessage(HttpStatus.BAD_REQUEST, message));
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Invalid Request.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RestErrorMessage(HttpStatus.BAD_REQUEST, message));
    }

    private ResponseEntity<RestErrorMessage> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new RestErrorMessage(status, message));
    }
}