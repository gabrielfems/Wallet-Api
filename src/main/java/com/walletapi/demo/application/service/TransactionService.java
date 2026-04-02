package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.TransactionDepositDTO;
import com.walletapi.demo.application.dto.TransactionTransferDTO;
import com.walletapi.demo.application.dto.TransactionWithdrawDTO;
import com.walletapi.demo.domain.entities.Transaction;
import com.walletapi.demo.domain.enums.TransactionType;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.Wallet;
import com.walletapi.demo.infrastructure.repositories.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionValidatorService validatorService;
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final ExecuteService executeService;

    @Transactional
    public Transaction createTransfer(TransactionTransferDTO data) {
        User sender = validatorService.validateSender(data.senderId());
        User receiver = validatorService.validateReceiver(data.receiverId());

        validatorService.validateTransfer(sender, data.amount());
        executeService.executeTransfer(sender, receiver, data.amount());

        Transaction transaction = buildTransaction(
                data.amount(),
                sender.getWallet(),
                sender, receiver
        );
        transaction.setType(TransactionType.TRANSFER);

        transactionRepository.save(transaction);
        userService.saveUser(sender);
        userService.saveUser(receiver);

        return transaction;
    }

    @Transactional
    public Transaction createDeposit(TransactionDepositDTO data) {
        User user = validatorService.validateSender(data.userId());

        executeService.executeDeposit(user, data.amount());

        Transaction transaction = buildTransaction(
                data.amount(),
                user.getWallet(),
                user,
                null);
        transaction.setType(TransactionType.DEPOSIT);

        transactionRepository.save(transaction);
        userService.saveUser(user);

        return transaction;
    }

    @Transactional
    public Transaction createWithdraw(TransactionWithdrawDTO data) {
        User user = validatorService.validateSender(data.senderId());

        validatorService.validateWithdraw(user, data.amount());
        executeService.executeWithdraw(user, data.amount());

        Transaction transaction = buildTransaction(
                data.amount(),
                user.getWallet(),
                user,
                null);
        transaction.setType(TransactionType.WITHDRAW);

        transactionRepository.save(transaction);
        userService.saveUser(user);

        return transaction;
    }

    private Transaction buildTransaction(BigDecimal amount, Wallet wallet, User sender, User receiver) {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setWallet(wallet);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        return transaction;
    }
}
