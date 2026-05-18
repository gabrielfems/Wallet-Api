package com.walletapi.demo.application.service;

import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.Wallet;
import com.walletapi.demo.domain.enums.WalletStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class ExecuteServiceTest {

    private ExecuteService executeService;

    private User sender;
    private User receiver;
    private Wallet senderWallet;
    private Wallet receiverWallet;

    @BeforeEach
    void setUp() {
        executeService = new ExecuteService();

        senderWallet = new Wallet();
        senderWallet.setBalance(new BigDecimal("1000.00"));
        senderWallet.setStatus(WalletStatus.ACTIVE);

        receiverWallet = new Wallet();
        receiverWallet.setBalance(new BigDecimal("500.00"));
        receiverWallet.setStatus(WalletStatus.ACTIVE);

        sender = new User();
        sender.setId(1L);
        sender.setName("João");
        sender.setWallet(senderWallet);

        receiver = new User();
        receiver.setId(2L);
        receiver.setName("Maria");
        receiver.setWallet(receiverWallet);
    }

    @Test
    @DisplayName("executeTransfer: deve subtrair o amount da carteira do sender")
    void executeTransfer_dadosValidos_subtraiDoSender() {
        executeService.executeTransfer(sender, receiver, new BigDecimal("300.00"));

        assertThat(senderWallet.getBalance()).isEqualByComparingTo(new BigDecimal("700.00"));
    }

    @Test
    @DisplayName("executeTransfer: deve adicionar o amount à carteira do receiver")
    void executeTransfer_dadosValidos_adicionaAoReceiver() {
        executeService.executeTransfer(sender, receiver, new BigDecimal("300.00"));

        assertThat(receiverWallet.getBalance()).isEqualByComparingTo(new BigDecimal("800.00"));
    }

    @Test
    @DisplayName("executeTransfer: deve preservar a soma total dos saldos após a transferência")
    void executeTransfer_dadosValidos_preservaSomaTotalDosSaldos() {
        BigDecimal totalAntes = senderWallet.getBalance().add(receiverWallet.getBalance());

        executeService.executeTransfer(sender, receiver, new BigDecimal("300.00"));

        BigDecimal totalDepois = senderWallet.getBalance().add(receiverWallet.getBalance());
        assertThat(totalDepois).isEqualByComparingTo(totalAntes);
    }

    @Test
    @DisplayName("executeDeposit: deve adicionar o amount ao saldo da carteira")
    void executeDeposit_dadosValidos_adicionaAoSaldo() {
        executeService.executeDeposit(sender, new BigDecimal("200.00"));

        assertThat(senderWallet.getBalance()).isEqualByComparingTo(new BigDecimal("1200.00"));
    }

    @Test
    @DisplayName("executeDeposit: deve funcionar com depósito de valor alto")
    void executeDeposit_valorAlto_adicionaCorretamente() {
        executeService.executeDeposit(sender, new BigDecimal("99999.99"));

        assertThat(senderWallet.getBalance()).isEqualByComparingTo(new BigDecimal("100999.99"));
    }

    @Test
    @DisplayName("executeWithdraw: deve subtrair o amount do saldo da carteira")
    void executeWithdraw_dadosValidos_subtraiDoSaldo() {
        executeService.executeWithdraw(sender, new BigDecimal("400.00"));

        assertThat(senderWallet.getBalance()).isEqualByComparingTo(new BigDecimal("600.00"));
    }

    @Test
    @DisplayName("executeWithdraw: deve zerar o saldo quando amount é igual ao saldo disponível")
    void executeWithdraw_amountIgualAoSaldo_zeraCarteira() {
        executeService.executeWithdraw(sender, new BigDecimal("1000.00"));

        assertThat(senderWallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}