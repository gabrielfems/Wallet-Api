package com.walletapi.demo.application.service;

import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.Wallet;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ExecuteService {

        public void executeTransfer (User sender, User receiver, BigDecimal amount){
            Wallet senderWallet = sender.getWallet();
            Wallet receiverWallet = receiver.getWallet();

            senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
            receiverWallet.setBalance(receiverWallet.getBalance().add(amount));
        }

        public void executeDeposit (User user, BigDecimal amount){
            user.getWallet().setBalance(user.getWallet().getBalance().add(amount));
        }

        public void executeWithdraw (User user, BigDecimal amount){
            user.getWallet().setBalance(user.getWallet().getBalance().subtract(amount));
        }

}
