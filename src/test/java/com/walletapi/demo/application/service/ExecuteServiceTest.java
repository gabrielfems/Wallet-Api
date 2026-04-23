package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.UserCreateDTO;
import com.walletapi.demo.application.exceptions.InsufficientBalanceException;
import com.walletapi.demo.domain.entities.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class ExecuteServiceTest {

    private ExecuteService executeService;

    @InjectMocks
    private TransactionValidatorService transactionValidatorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        executeService = new ExecuteService();
    }

    @Test
    @DisplayName("Should execute a transfer when everything is OK")
    void executeTransferCase1() {
                UserCreateDTO sender = new UserCreateDTO("Gabriel",
                "gabriel.fe132@gmail.com",
                "123@pass",
                "44974002293",
                "87080283",
                "123",
                "b",
                LocalDate.of(2002, 02, 24),
                "1234567890");

        UserCreateDTO receiver = new UserCreateDTO("Gabriel",
                "gabriel.fe132@gmail.com",
                "123@pass",
                "44974002293",
                "87080283",
                "123",
                "b",
                LocalDate.of(2002, 02, 24),
                "1234567890");

        User senderUser = new User(sender);
        User receiverUser = new User(receiver);

        senderUser.getWallet().setBalance(BigDecimal.valueOf(300));
        receiverUser.getWallet().setBalance(BigDecimal.valueOf(100));

        BigDecimal amount = BigDecimal.valueOf(200);

        executeService.executeTransfer(senderUser, receiverUser, amount);

        assertThat(senderUser.getWallet().getBalance())
                .isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(receiverUser.getWallet().getBalance())
                .isEqualByComparingTo(BigDecimal.valueOf(300));

    }

    @Test
    @DisplayName("Should throw exception when sender balance is insufficient")
    void executeTransferCase2() {
        UserCreateDTO sender = new UserCreateDTO("Gabriel",
                "gabriel.fe132@gmail.com",
                "123@pass",
                "44974002293",
                "87080283",
                "123",
                "b",
                LocalDate.of(2002, 02, 24),
                "1234567890");

        UserCreateDTO receiver = new UserCreateDTO("Gabriel",
                "gabriel.fe132@gmail.com",
                "123@pass",
                "44974002293",
                "87080283",
                "123",
                "b",
                LocalDate.of(2002, 02, 24),
                "1234567890");

        User senderUser = new User(sender);
        User receiverUser = new User(receiver);

        senderUser.getWallet().setBalance(BigDecimal.valueOf(300));
        receiverUser.getWallet().setBalance(BigDecimal.valueOf(100));

        BigDecimal amount = BigDecimal.valueOf(400);

        Exception thrown = Assertions.assertThrows(InsufficientBalanceException.class, () -> {
            transactionValidatorService.validateTransfer(senderUser, amount);
        });

        Assertions.assertEquals("Insufficient funds", thrown.getMessage());

        assertThat(senderUser.getWallet().getBalance())
                .isEqualByComparingTo("300");

        assertThat(receiverUser.getWallet().getBalance())
                .isEqualByComparingTo("100");

    }

    @Test
    void executeDeposit() {
    }

    @Test
    void executeWithdraw() {
    }
}