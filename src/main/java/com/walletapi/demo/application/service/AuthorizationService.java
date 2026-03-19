package com.walletapi.demo.application.service;

import com.walletapi.demo.application.exceptions.AmountException;
import com.walletapi.demo.application.exceptions.BalanceException;
import com.walletapi.demo.application.exceptions.SenderUserNotFoundException;
import com.walletapi.demo.application.exceptions.ReceiverUserNotFoundException;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.infrastructure.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AuthorizationService {

    @Autowired
    private UserRepository userRepository;

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
            throw new BalanceException();
        }
    }

    public void validateWithdraw(User fromUser, BigDecimal amount) {
        if (fromUser.getWallet().getBalance().compareTo(amount) < 0) {
            throw new AmountException();
        }
    }
}