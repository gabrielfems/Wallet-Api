package com.walletapi.demo.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walletapi.demo.application.dto.TransactionDepositDTO;
import com.walletapi.demo.application.dto.TransactionTransferDTO;
import com.walletapi.demo.application.dto.TransactionWithdrawDTO;
import com.walletapi.demo.application.exceptions.InsufficientBalanceException;
import com.walletapi.demo.application.exceptions.ReceiverUserNotFoundException;
import com.walletapi.demo.application.exceptions.SenderUserNotFoundException;
import com.walletapi.demo.application.service.TransactionService;
import com.walletapi.demo.domain.entities.Transaction;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    TransactionService transactionService;

    private Transaction transaction;
    private User sender;
    private User receiver;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setName("João");

        receiver = new User();
        receiver.setName("Maria");

        transaction = new Transaction();
    }

    @Test
    @DisplayName("Should return 200 when transfer is successful")
    void createTransferCase1()  throws Exception {
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(BigDecimal.valueOf(10000));
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setId(1L);

        TransactionTransferDTO dto = new TransactionTransferDTO(BigDecimal.valueOf(10000), 1L, 2L);

        when(transactionService.createTransfer(dto)).thenReturn(transaction);

        mockMvc.perform(post("/api/users/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderName").value("João"))
                .andExpect(jsonPath("$.receiverName").value("Maria"))
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.amount").value(10000));

        verify(transactionService).createTransfer(dto);

    }

    @Test
    @DisplayName("Should return 400 when amount is null")
    void createTransferCase2()  throws Exception {
        TransactionTransferDTO dto = new TransactionTransferDTO(null, 1L, 2L);

        mockMvc.perform(post("/api/users/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("Should return 400 when amount is zero")
    void createTransferCase3()  throws Exception {
        TransactionTransferDTO dto = new TransactionTransferDTO(BigDecimal.ZERO, 1L, 2L);

        mockMvc.perform(post("/api/users/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("Should return 400 when amount is negative")
    void createTransferCase4()  throws Exception {
        TransactionTransferDTO dto = new TransactionTransferDTO(BigDecimal.valueOf(-1), 1L, 2L);

        mockMvc.perform(post("/api/users/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("Should return 400 when sender is null")
    void createTransferCase5()  throws Exception {
        TransactionTransferDTO dto = new TransactionTransferDTO(BigDecimal.valueOf(1000), null, 2L);

        mockMvc.perform(post("/api/users/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("Should return 400 when receiver is null")
    void createTransferCase6()  throws Exception {
        TransactionTransferDTO dto = new TransactionTransferDTO(BigDecimal.valueOf(1000), 1L, null);

        mockMvc.perform(post("/api/users/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("Should return 404 when sender not found")
    void createTransferCase7()  throws Exception {
        TransactionTransferDTO dto = new TransactionTransferDTO(BigDecimal.valueOf(1000), 99L, 2L);

        when(transactionService.createTransfer(dto)).thenThrow(new SenderUserNotFoundException());

        mockMvc.perform(post("/api/users/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(transactionService).createTransfer(dto);
    }

    @Test
    @DisplayName("Should return 404 when receiver not found")
    void createTransferCase8()  throws Exception {
        TransactionTransferDTO dto = new TransactionTransferDTO(BigDecimal.valueOf(1000), 1L, 99L);

        when(transactionService.createTransfer(dto)).thenThrow(new ReceiverUserNotFoundException());

        mockMvc.perform(post("/api/users/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(transactionService).createTransfer(dto);
    }

    @Test
    @DisplayName("Should return 422 when insufficient funds")
    void createTransferCase9()  throws Exception {
        TransactionTransferDTO dto = new TransactionTransferDTO(BigDecimal.valueOf(1000), 1L, 2L);

        when(transactionService.createTransfer(dto)).thenThrow(new InsufficientBalanceException());

        mockMvc.perform(post("/api/users/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());

        verify(transactionService).createTransfer(dto);
    }

    @Test
    @DisplayName("Should return 200 when deposit is successful")
    void createDepositCase1() throws Exception{
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(BigDecimal.valueOf(10000));
        transaction.setSender(sender);
        transaction.setId(1L);

        TransactionDepositDTO dto = new TransactionDepositDTO(BigDecimal.valueOf(10000), 1L);

        when(transactionService.createDeposit(dto)).thenReturn(transaction);

        mockMvc.perform(post("/api/users/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderName").value("João"))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(10000));

        verify(transactionService).createDeposit(dto);
    }

    @Test
    @DisplayName("Should return 400 when amount is null")
    void createDepositCase2()  throws Exception {
        TransactionDepositDTO dto = new TransactionDepositDTO(null, 1L);

        mockMvc.perform(post("/api/users/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("Should return 400 when amount is zero")
    void createDepositCase3()  throws Exception {
        TransactionDepositDTO dto = new TransactionDepositDTO(BigDecimal.ZERO, 1L);

        mockMvc.perform(post("/api/users/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("Should return 400 when amount is negative")
    void createDepositCase4()  throws Exception {
        TransactionDepositDTO dto = new TransactionDepositDTO(BigDecimal.valueOf(-1), 1L);

        mockMvc.perform(post("/api/users/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("Should return 400 when user is null")
    void createDepositCase5()  throws Exception {
        TransactionDepositDTO dto = new TransactionDepositDTO(BigDecimal.valueOf(1000), null);

        mockMvc.perform(post("/api/users/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("Should return 404 when sender not found")
    void createDepositCase6()  throws Exception {
        TransactionDepositDTO dto = new TransactionDepositDTO(BigDecimal.valueOf(1000), 99L);

        when(transactionService.createDeposit(dto)).thenThrow(new SenderUserNotFoundException());

        mockMvc.perform(post("/api/users/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(transactionService).createDeposit(dto);
    }

    @Test
    @DisplayName("Should return 200 when withdraw is successful")
    void createWithdrawCase1() throws Exception {
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setAmount(BigDecimal.valueOf(10000));
        transaction.setSender(sender);
        transaction.setId(1L);

        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(BigDecimal.valueOf(10000), 1L);

        when(transactionService.createWithdraw(dto)).thenReturn(transaction);

        mockMvc.perform(post("/api/users/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderName").value("João"))
                .andExpect(jsonPath("$.type").value("WITHDRAW"))
                .andExpect(jsonPath("$.amount").value(10000));

        verify(transactionService).createWithdraw(dto);
    }

    @Test
    @DisplayName("Should return 400 when amount is null")
    void createWithdrawCase2()  throws Exception {
        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(null, 1L);

        mockMvc.perform(post("/api/users/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("Should return 400 when amount is zero")
    void createWithdrawCase3()  throws Exception {
        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(BigDecimal.ZERO, 1L);

        mockMvc.perform(post("/api/users/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("Should return 400 when amount is negative")
    void createWithdrawCase4()  throws Exception {
        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(BigDecimal.valueOf(-1), 1L);

        mockMvc.perform(post("/api/users/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("Should return 400 when user is null")
    void createWithdrawCase5()  throws Exception {
        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(BigDecimal.valueOf(1000), null);

        mockMvc.perform(post("/api/users/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("Should return 404 when sender not found")
    void createWithdrawCase6()  throws Exception {
        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(BigDecimal.valueOf(1000), 99L);

        when(transactionService.createWithdraw(dto)).thenThrow(new SenderUserNotFoundException());

        mockMvc.perform(post("/api/users/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(transactionService).createWithdraw(dto);
    }

    @Test
    @DisplayName("Should return 422 when insufficient funds")
    void createWithdrawCase7()  throws Exception {
        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(BigDecimal.valueOf(1000), 1L);

        when(transactionService.createWithdraw(dto)).thenThrow(new InsufficientBalanceException());

        mockMvc.perform(post("/api/users/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());

        verify(transactionService).createWithdraw(dto);
    }

}