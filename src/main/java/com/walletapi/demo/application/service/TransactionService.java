package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.TransactionDepositDTO;
import com.walletapi.demo.application.dto.TransactionTransferDTO;
import com.walletapi.demo.application.dto.TransactionWithdrawDTO;
import com.walletapi.demo.domain.entities.Transaction;
import com.walletapi.demo.domain.enums.TransactionType;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.Wallet;
import com.walletapi.demo.infrastructure.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AuthorizationService authService;
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final ExecuteService executeService;

    public Transaction createTransfer(TransactionTransferDTO data) {
        User sender = authService.validateSender(data.senderId());
        User receiver = authService.validateReceiver(data.receiverId());

        authService.validateTransfer(sender, data.amount());
        executeService.executeTransfer(sender, receiver, data.amount());

        Transaction transaction = buildTransaction(
                data.amount(),
                data.type(),
                sender.getWallet(),
                sender, receiver
        );

        transactionRepository.save(transaction);
        userService.saveUser(sender);
        userService.saveUser(receiver);

        return transaction;
    }

    public Transaction createDeposit(TransactionDepositDTO data) {
        User user = authService.validateSender(data.senderId());

        executeService.executeDeposit(user, data.amount());

        Transaction transaction = buildTransaction(data.amount(), data.type(), user.getWallet(), user, null);
        transactionRepository.save(transaction);
        userService.saveUser(user);

        return transaction;
    }

    public Transaction createWithdraw(TransactionWithdrawDTO data) {
        User user = authService.validateSender(data.senderId());

        authService.validateWithdraw(user, data.amount());
        executeService.executeWithdraw(user, data.amount());

        Transaction transaction = buildTransaction(data.amount(), data.type(), user.getWallet(), user, null);
        transactionRepository.save(transaction);
        userService.saveUser(user);

        return transaction;
    }

    public Transaction buildTransaction(BigDecimal amount, TransactionType type, Wallet wallet, User sender, User receiver) {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setWallet(wallet);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        return transaction;
    }
}
