package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.UserCreateDTO;
import com.walletapi.demo.application.exceptions.InsufficientBalanceException;
import com.walletapi.demo.application.exceptions.ReceiverUserNotFoundException;
import com.walletapi.demo.application.exceptions.SenderUserNotFoundException;
import com.walletapi.demo.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionValidatorServiceTest {

    @InjectMocks
    TransactionValidatorService transactionValidatorService;

    @Mock
    private UserService userService;

    private User userSender;
    private User userReceiver;

    @BeforeEach
    void setUp() {
        UserCreateDTO dto = new UserCreateDTO(
                "Gabriel", "gabriel@gmail.com", "123@pass",
                "44974002293", "87080078", "123", "b",
                LocalDate.of(2002, 12, 20), "1234567890");

        userSender = new User(dto);
        userReceiver = new User(dto);
    }

    @Test
    @DisplayName("Should return a valid Sender")
    void validateSenderCase1() {

        userSender.setId(1L);

        when(userService.findSenderById(1L)).thenReturn(userSender);

        User result = transactionValidatorService.validateSender(1L);

        assertThat(result).isEqualTo(userSender);
    }

    @Test
    @DisplayName("Should throw a exception when Sender not found")
    void validateSenderCase2() {

        when(userService.findSenderById(1L))
                .thenThrow(new SenderUserNotFoundException());

        assertThatThrownBy(() -> transactionValidatorService.validateSender(1L))
                .isInstanceOf(SenderUserNotFoundException.class);
    }

    @Test
    @DisplayName("Should return a valid receiver")
    void validateReceiverCase1() {

        userReceiver.setId(1L);

        when(userService.findReceiverById(1L)).thenReturn(userReceiver);

        User result = transactionValidatorService.validateReceiver(1L);

        assertThat(result).isEqualTo(userReceiver);
    }

    @Test
    @DisplayName("Should throw a exception when Receiver not found")
    void validateReceiverCase2() {

        when(userService.findReceiverById(1L))
                .thenThrow(new ReceiverUserNotFoundException());

        assertThatThrownBy(() -> transactionValidatorService.validateReceiver(1L))
                .isInstanceOf(ReceiverUserNotFoundException.class);

    }

    @Test
    @DisplayName("Should return a valid transaction because balance is sufficient")
    void validateTransferCase1() {

        userSender.getWallet().setBalance(BigDecimal.valueOf(100));

        BigDecimal amount = BigDecimal.valueOf(50);

        assertThatCode(() -> transactionValidatorService.validateTransfer(userSender, amount))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw a exception when balance is less than amount")
    void validateTransferCase2() {

        userSender.getWallet().setBalance(BigDecimal.valueOf(30));

        BigDecimal amount = BigDecimal.valueOf(50);

        assertThatThrownBy(() -> transactionValidatorService.validateTransfer(userSender, amount))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("Should return a valid withdraw when balance is sufficient")
    void validateWithdrawCase1() {

        userSender.getWallet().setBalance(BigDecimal.valueOf(100));

        BigDecimal amount = BigDecimal.valueOf(50);

        assertThatCode(() -> transactionValidatorService.validateWithdraw(userSender, amount))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw a exception when balance is less than amount")
    void validateWithdrawCase2() {

        userSender.getWallet().setBalance(BigDecimal.valueOf(30));

        BigDecimal amount = BigDecimal.valueOf(50);

        assertThatThrownBy(() -> transactionValidatorService.validateWithdraw(userSender, amount))
                .isInstanceOf(InsufficientBalanceException.class);
    }
}