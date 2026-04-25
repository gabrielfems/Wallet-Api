package com.walletapi.demo.application.service;

import com.walletapi.demo.application.exceptions.InsufficientBalanceException;
import com.walletapi.demo.domain.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionValidatorService {

    private final UserService userService;

    public User validateSender(Long fromUserId) {
        return userService.findSenderById(fromUserId);
    }

    public User validateReceiver(Long toUserId) {
        return userService.findReceiverById(toUserId);
    }

    public void validateTransfer(User fromUser, BigDecimal amount) {
        if (fromUser.getWallet().getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
    }

    public void validateWithdraw(User fromUser, BigDecimal amount) {
        if (fromUser.getWallet().getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
    }
}