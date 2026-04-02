package com.walletapi.demo.application.service;

import com.walletapi.demo.application.exceptions.InsufficientBalanceException;
import com.walletapi.demo.application.exceptions.ReceiverUserNotFoundException;
import com.walletapi.demo.application.exceptions.SenderUserNotFoundException;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.infrastructure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionValidatorService {

    private final UserRepository userRepository;

    public User validateSender(Long fromUserId) {
        return userRepository.findById(fromUserId)
                .orElseThrow(SenderUserNotFoundException::new);
    }

    public User validateReceiver(Long toUserId) {
        return userRepository.findById(toUserId)
                .orElseThrow(ReceiverUserNotFoundException::new);
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