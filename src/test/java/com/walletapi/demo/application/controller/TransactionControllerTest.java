package com.walletapi.demo.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walletapi.demo.application.dto.TransactionDepositDTO;
import com.walletapi.demo.application.dto.TransactionTransferDTO;
import com.walletapi.demo.application.dto.TransactionWithdrawDTO;
import com.walletapi.demo.application.exceptions.InsufficientBalanceException;
import com.walletapi.demo.application.exceptions.ReceiverUserNotFoundException;
import com.walletapi.demo.application.service.TokenService;
import com.walletapi.demo.application.service.TransactionService;
import com.walletapi.demo.domain.entities.Transaction;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.UserCredentials;
import com.walletapi.demo.domain.entities.Wallet;
import com.walletapi.demo.domain.enums.TransactionType;
import com.walletapi.demo.domain.enums.UserRole;
import com.walletapi.demo.domain.enums.WalletStatus;
import com.walletapi.demo.infrastructure.config.SecurityConfigurations;
import com.walletapi.demo.infrastructure.repositories.UserCredentialsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import(SecurityConfigurations.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserCredentialsRepository userCredentialsRepository;

    private static final String FAKE_TOKEN = "Bearer fake-jwt-token";

    private User sender;
    private User receiver;
    private UserCredentials credentials;

    @BeforeEach
    void setUp() {
        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal("1000.00"));
        wallet.setStatus(WalletStatus.ACTIVE);

        sender = new User();
        sender.setId(1L);
        sender.setName("João");
        sender.setWallet(wallet);

        receiver = new User();
        receiver.setId(2L);
        receiver.setName("Maria");

        credentials = new UserCredentials();
        credentials.setId("uuid-teste");
        credentials.setLogin("gabriel@email.com");
        credentials.setPassword("senha_hash");
        credentials.setRole(UserRole.USER);
        credentials.setUser(sender);

        when(tokenService.validateToken("fake-jwt-token")).thenReturn("gabriel@email.com");
        when(userCredentialsRepository.findByLogin("gabriel@email.com")).thenReturn(credentials);
    }

    @Test
    @DisplayName("createTransfer: deve retornar 200 e o DTO da transação quando dados são válidos")
    void createTransfer_dadosValidos_retorna200() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(new BigDecimal("1000.00"));
        transaction.setSender(sender);
        transaction.setReceiver(receiver);

        TransactionTransferDTO dto = new TransactionTransferDTO(new BigDecimal("1000.00"), 2L);

        when(transactionService.createTransfer(any(User.class), any(TransactionTransferDTO.class)))
                .thenReturn(transaction);

        mockMvc.perform(post("/api/users/transactions/transfer")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderName").value("João"))
                .andExpect(jsonPath("$.receiverName").value("Maria"))
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.amount").value(1000.00));

        verify(transactionService).createTransfer(any(User.class), any(TransactionTransferDTO.class));
    }

    @Test
    @DisplayName("createTransfer: deve retornar 400 quando amount é nulo")
    void createTransfer_amountNulo_retorna400() throws Exception {
        TransactionTransferDTO dto = new TransactionTransferDTO(null, 2L);

        mockMvc.perform(post("/api/users/transactions/transfer")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("createTransfer: deve retornar 400 quando amount é zero")
    void createTransfer_amountZero_retorna400() throws Exception {
        TransactionTransferDTO dto = new TransactionTransferDTO(BigDecimal.ZERO, 2L);

        mockMvc.perform(post("/api/users/transactions/transfer")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("createTransfer: deve retornar 400 quando amount é negativo")
    void createTransfer_amountNegativo_retorna400() throws Exception {
        TransactionTransferDTO dto = new TransactionTransferDTO(new BigDecimal("-1.00"), 2L);

        mockMvc.perform(post("/api/users/transactions/transfer")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("createTransfer: deve retornar 400 quando receiverId é nulo")
    void createTransfer_receiverIdNulo_retorna400() throws Exception {
        TransactionTransferDTO dto = new TransactionTransferDTO(new BigDecimal("1000.00"), null);

        mockMvc.perform(post("/api/users/transactions/transfer")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("createTransfer: deve retornar 404 quando receptor não existe")
    void createTransfer_receptorNaoEncontrado_retorna404() throws Exception {
        TransactionTransferDTO dto = new TransactionTransferDTO(new BigDecimal("1000.00"), 99L);

        when(transactionService.createTransfer(any(User.class), any(TransactionTransferDTO.class)))
                .thenThrow(new ReceiverUserNotFoundException());

        mockMvc.perform(post("/api/users/transactions/transfer")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(transactionService).createTransfer(any(User.class), any(TransactionTransferDTO.class));
    }

    @Test
    @DisplayName("createTransfer: deve retornar 422 quando saldo é insuficiente")
    void createTransfer_saldoInsuficiente_retorna422() throws Exception {
        TransactionTransferDTO dto = new TransactionTransferDTO(new BigDecimal("9999.00"), 2L);

        when(transactionService.createTransfer(any(User.class), any(TransactionTransferDTO.class)))
                .thenThrow(new InsufficientBalanceException());

        mockMvc.perform(post("/api/users/transactions/transfer")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());

        verify(transactionService).createTransfer(any(User.class), any(TransactionTransferDTO.class));
    }

    @Test
    @DisplayName("createDeposit: deve retornar 200 e o DTO da transação quando dados são válidos")
    void createDeposit_dadosValidos_retorna200() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(new BigDecimal("1000.00"));
        transaction.setSender(sender);

        TransactionDepositDTO dto = new TransactionDepositDTO(new BigDecimal("1000.00"));

        when(transactionService.createDeposit(any(User.class), any(TransactionDepositDTO.class)))
                .thenReturn(transaction);

        mockMvc.perform(post("/api/users/transactions/deposit")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderName").value("João"))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(1000.00));

        verify(transactionService).createDeposit(any(User.class), any(TransactionDepositDTO.class));
    }

    @Test
    @DisplayName("createDeposit: deve retornar 400 quando amount é nulo")
    void createDeposit_amountNulo_retorna400() throws Exception {
        TransactionDepositDTO dto = new TransactionDepositDTO(null);

        mockMvc.perform(post("/api/users/transactions/deposit")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("createDeposit: deve retornar 400 quando amount é zero")
    void createDeposit_amountZero_retorna400() throws Exception {
        TransactionDepositDTO dto = new TransactionDepositDTO(BigDecimal.ZERO);

        mockMvc.perform(post("/api/users/transactions/deposit")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("createDeposit: deve retornar 400 quando amount é negativo")
    void createDeposit_amountNegativo_retorna400() throws Exception {
        TransactionDepositDTO dto = new TransactionDepositDTO(new BigDecimal("-1.00"));

        mockMvc.perform(post("/api/users/transactions/deposit")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("createWithdraw: deve retornar 200 e o DTO da transação quando dados são válidos")
    void createWithdraw_dadosValidos_retorna200() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setAmount(new BigDecimal("1000.00"));
        transaction.setSender(sender);

        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(new BigDecimal("1000.00"));

        when(transactionService.createWithdraw(any(User.class), any(TransactionWithdrawDTO.class)))
                .thenReturn(transaction);

        mockMvc.perform(post("/api/users/transactions/withdraw")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderName").value("João"))
                .andExpect(jsonPath("$.type").value("WITHDRAW"))
                .andExpect(jsonPath("$.amount").value(1000.00));

        verify(transactionService).createWithdraw(any(User.class), any(TransactionWithdrawDTO.class));
    }

    @Test
    @DisplayName("createWithdraw: deve retornar 400 quando amount é nulo")
    void createWithdraw_amountNulo_retorna400() throws Exception {
        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(null);

        mockMvc.perform(post("/api/users/transactions/withdraw")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("createWithdraw: deve retornar 400 quando amount é zero")
    void createWithdraw_amountZero_retorna400() throws Exception {
        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(BigDecimal.ZERO);

        mockMvc.perform(post("/api/users/transactions/withdraw")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("createWithdraw: deve retornar 400 quando amount é negativo")
    void createWithdraw_amountNegativo_retorna400() throws Exception {
        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(new BigDecimal("-1.00"));

        mockMvc.perform(post("/api/users/transactions/withdraw")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("createWithdraw: deve retornar 422 quando saldo é insuficiente")
    void createWithdraw_saldoInsuficiente_retorna422() throws Exception {
        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(new BigDecimal("9999.00"));

        when(transactionService.createWithdraw(any(User.class), any(TransactionWithdrawDTO.class)))
                .thenThrow(new InsufficientBalanceException());

        mockMvc.perform(post("/api/users/transactions/withdraw")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());

        verify(transactionService).createWithdraw(any(User.class), any(TransactionWithdrawDTO.class));
    }
}