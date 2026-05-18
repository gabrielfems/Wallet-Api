package com.walletapi.demo.application.service;

import com.walletapi.demo.application.exceptions.InsufficientBalanceException;
import com.walletapi.demo.application.exceptions.ReceiverUserNotFoundException;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.Wallet;
import com.walletapi.demo.domain.enums.WalletStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionValidatorServiceTest {

    @InjectMocks
    private TransactionValidatorService validatorService;

    @Mock
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal("1000.00"));
        wallet.setStatus(WalletStatus.ACTIVE);

        user = new User();
        user.setId(1L);
        user.setName("Gabriel");
        user.setWallet(wallet);
    }

    @Test
    @DisplayName("validateReceiver: deve retornar o receptor quando o id existe")
    void validateReceiver_idExiste_retornaUsuario() {
        when(userService.findReceiverById(1L)).thenReturn(user);

        User result = validatorService.validateReceiver(1L);

        assertThat(result).isEqualTo(user);
        verify(userService).findReceiverById(1L);
    }

    @Test
    @DisplayName("validateReceiver: deve lançar ReceiverUserNotFoundException quando o id não existe")
    void validateReceiver_idNaoExiste_lancaReceiverUserNotFoundException() {
        when(userService.findReceiverById(99L)).thenThrow(new ReceiverUserNotFoundException());

        assertThatThrownBy(() -> validatorService.validateReceiver(99L))
                .isInstanceOf(ReceiverUserNotFoundException.class);

        verify(userService).findReceiverById(99L);
    }

    @Test
    @DisplayName("validateTransfer: deve passar sem exceção quando saldo é suficiente")
    void validateTransfer_saldoSuficiente_semExcecao() {
        assertThatNoException()
                .isThrownBy(() -> validatorService.validateTransfer(user, new BigDecimal("500.00")));
    }

    @Test
    @DisplayName("validateTransfer: deve passar sem exceção quando amount é exatamente igual ao saldo")
    void validateTransfer_amountIgualAoSaldo_semExcecao() {
        assertThatNoException()
                .isThrownBy(() -> validatorService.validateTransfer(user, new BigDecimal("1000.00")));
    }

    @Test
    @DisplayName("validateTransfer: deve lançar InsufficientBalanceException quando saldo é insuficiente")
    void validateTransfer_saldoInsuficiente_lancaInsufficientBalanceException() {
        assertThatThrownBy(() -> validatorService.validateTransfer(user, new BigDecimal("1000.01")))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("validateWithdraw: deve passar sem exceção quando saldo é suficiente")
    void validateWithdraw_saldoSuficiente_semExcecao() {
        assertThatNoException()
                .isThrownBy(() -> validatorService.validateWithdraw(user, new BigDecimal("300.00")));
    }

    @Test
    @DisplayName("validateWithdraw: deve passar sem exceção quando amount é exatamente igual ao saldo")
    void validateWithdraw_amountIgualAoSaldo_semExcecao() {
        assertThatNoException()
                .isThrownBy(() -> validatorService.validateWithdraw(user, new BigDecimal("1000.00")));
    }

    @Test
    @DisplayName("validateWithdraw: deve lançar InsufficientBalanceException quando saldo é insuficiente")
    void validateWithdraw_saldoInsuficiente_lancaInsufficientBalanceException() {
        assertThatThrownBy(() -> validatorService.validateWithdraw(user, new BigDecimal("1000.01")))
                .isInstanceOf(InsufficientBalanceException.class);
    }
}