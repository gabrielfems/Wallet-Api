package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.UserCreateDTO;
import com.walletapi.demo.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ExecuteServiceTest {

    private ExecuteService executeService;

    User senderUser;
    User receiverUser;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        executeService = new ExecuteService();

        UserCreateDTO sender = new UserCreateDTO("Gabriel",
                "gabriel@gmail.com",
                "123@pass",
                "44974002293",
                "87080078",
                "123",
                "b",
                LocalDate.of(2002, 12, 20),
                "1234567890");

        UserCreateDTO receiver = new UserCreateDTO("Gabriel",
                "gabriel@gmail.com",
                "123@pass",
                "44974002293",
                "87080078",
                "123",
                "b",
                LocalDate.of(2002, 12, 20),
                "1234567890");

        senderUser = new User(sender);
        receiverUser = new User(receiver);
    }

    @Test
    @DisplayName("Should execute a transfer when everything is OK")
    void executeTransferCase1() {
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
    @DisplayName("Should execute the deposit when everything is OK")
    void executeDepositCase1() {
        senderUser.setId(1L);
        senderUser.getWallet().setBalance(BigDecimal.valueOf(100));

        executeService.executeDeposit(senderUser, BigDecimal.valueOf(100));

        assertThat(senderUser.getWallet().getBalance())
        .isEqualByComparingTo("200");

    }

    @Test
    @DisplayName("Should execute the withdraw when everything is OK")
    void executeWithdrawCase1() {
        senderUser.setId(1L);
        senderUser.getWallet().setBalance(BigDecimal.valueOf(100));

        executeService.executeWithdraw(senderUser, BigDecimal.valueOf(100));

        assertThat(senderUser.getWallet().getBalance())
                .isEqualByComparingTo("0");
    }
}