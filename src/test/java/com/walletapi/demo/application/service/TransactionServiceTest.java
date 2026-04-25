package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.TransactionDepositDTO;
import com.walletapi.demo.application.dto.TransactionTransferDTO;
import com.walletapi.demo.application.dto.TransactionWithdrawDTO;
import com.walletapi.demo.application.dto.UserCreateDTO;
import com.walletapi.demo.application.exceptions.InsufficientBalanceException;
import com.walletapi.demo.application.exceptions.ReceiverUserNotFoundException;
import com.walletapi.demo.application.exceptions.SenderUserNotFoundException;
import com.walletapi.demo.domain.entities.Transaction;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.enums.TransactionType;
import com.walletapi.demo.infrastructure.repositories.TransactionRepository;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    TransactionService transactionService;

    @Mock
    TransactionValidatorService transactionValidatorService;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    UserService userService;

    @Mock
    ExecuteService executeService;

    private User userSender;
    private User userReceiver;

    @BeforeEach
    void setUp() {
        UserCreateDTO dto1 = new UserCreateDTO(
                "Gabriel", "gabriel@gmail.com", "123@pass",
                "44974002293", "87080078", "123", "b",
                LocalDate.of(2002, 12, 20), "1234567890");

        UserCreateDTO dto2 = new UserCreateDTO(
                "José", "ze@gmail.com", "123@pass",
                "44974002323", "87009078", "133", "a",
                LocalDate.of(2003, 2, 26), "1234567890");

        userSender = new User(dto1);
        userSender.setId(1L);

        userReceiver = new User(dto2);
        userReceiver.setId(2L);
    }

    @Test
    @DisplayName("Should create the transfer when everything is ok")
    void createTransferCase1() {
        TransactionTransferDTO dto = new TransactionTransferDTO( BigDecimal.valueOf(150),1L, 2L);

        when(transactionValidatorService.validateSender(1L)).thenReturn(userSender);
        when(transactionValidatorService.validateReceiver(2L)).thenReturn(userReceiver);

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.createTransfer(dto);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150));

        verify(transactionValidatorService, times(1)).validateTransfer(userSender, dto.amount());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw an exception because balance is insufficient")
    void createTransferCase2() {
        TransactionTransferDTO dto = new TransactionTransferDTO(BigDecimal.valueOf(150), 1L, 2L);

        when(transactionValidatorService.validateSender(1L)).thenReturn(userSender);
        when(transactionValidatorService.validateReceiver(2L)).thenReturn(userReceiver);
        doThrow(new InsufficientBalanceException())
                .when(transactionValidatorService).validateTransfer(userSender, dto.amount());

        assertThatThrownBy(() -> transactionService.createTransfer(dto))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("Should throw an exception when sender is not found")
    void createTransferCase3() {
        TransactionTransferDTO dto = new TransactionTransferDTO(BigDecimal.valueOf(150), 1L, 2L);

        doThrow(new SenderUserNotFoundException())
                .when(transactionValidatorService).validateSender(1L);

        assertThatThrownBy(() -> transactionService.createTransfer(dto))
                .isInstanceOf(SenderUserNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw an exception when receiver is not found")
    void createTransferCase4() {
        TransactionTransferDTO dto = new TransactionTransferDTO(BigDecimal.valueOf(150), 1L, 2L);

        when(transactionValidatorService.validateSender(1L)).thenReturn(userSender);
        doThrow(new ReceiverUserNotFoundException())
                .when(transactionValidatorService).validateReceiver(2L);

        assertThatThrownBy(() -> transactionService.createTransfer(dto))
                .isInstanceOf(ReceiverUserNotFoundException.class);
    }

    @Test
    @DisplayName("Should create the deposit when everything is ok")
    void createDepositCase1() {
        TransactionDepositDTO dto = new TransactionDepositDTO(BigDecimal.valueOf(150),1L);

        when(transactionValidatorService.validateSender(1L)).thenReturn(userSender);

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.createDeposit(dto);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150));

        verify(executeService, times(1)).executeDeposit(userSender, dto.amount());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw an exception when sender is not found")
    void createDepositCase2() {
        TransactionDepositDTO dto = new TransactionDepositDTO(BigDecimal.valueOf(150), 1L);

        doThrow(new SenderUserNotFoundException())
                .when(transactionValidatorService).validateSender(1L);

        assertThatThrownBy(() -> transactionService.createDeposit(dto))
                .isInstanceOf(SenderUserNotFoundException.class);
    }

    @Test
    @DisplayName("Should create the withdraw when everything is ok")
    void createWithdrawCase1() {
        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(BigDecimal.valueOf(150),1L);

        when(transactionValidatorService.validateSender(1L)).thenReturn(userSender);

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.createWithdraw(dto);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(TransactionType.WITHDRAW);
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150));

        verify(executeService, times(1)).executeWithdraw(userSender, dto.amount());
        verify(transactionValidatorService, times(1)).validateWithdraw(userSender, dto.amount());
        verify(transactionRepository).save(any(Transaction.class));
    }
}