package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.TransactionDepositDTO;
import com.walletapi.demo.application.dto.TransactionTransferDTO;
import com.walletapi.demo.application.dto.TransactionWithdrawDTO;
import com.walletapi.demo.application.exceptions.InsufficientBalanceException;
import com.walletapi.demo.application.exceptions.ReceiverUserNotFoundException;
import com.walletapi.demo.domain.entities.Transaction;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.Wallet;
import com.walletapi.demo.domain.enums.TransactionType;
import com.walletapi.demo.domain.enums.WalletStatus;
import com.walletapi.demo.infrastructure.repositories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionValidatorService validatorService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @Mock
    private ExecuteService executeService;

    private User sender;
    private User receiver;
    private Wallet senderWallet;
    private Wallet receiverWallet;

    @BeforeEach
    void setUp() {
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
    @DisplayName("createTransfer: deve criar transação de transferência e salvar quando dados são válidos")
    void createTransfer_dadosValidos_criaTrasacaoESalva() {
        TransactionTransferDTO dto = new TransactionTransferDTO(new BigDecimal("300.00"), 2L);

        when(validatorService.validateReceiver(2L)).thenReturn(receiver);
        doNothing().when(validatorService).validateTransfer(sender, new BigDecimal("300.00"));
        doNothing().when(executeService).executeTransfer(sender, receiver, new BigDecimal("300.00"));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.createTransfer(sender, dto);

        assertThat(result.getType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(result.getSender()).isEqualTo(sender);
        assertThat(result.getReceiver()).isEqualTo(receiver);

        verify(validatorService).validateReceiver(2L);
        verify(validatorService).validateTransfer(sender, new BigDecimal("300.00"));
        verify(executeService).executeTransfer(sender, receiver, new BigDecimal("300.00"));
        verify(transactionRepository).save(any(Transaction.class));
        verify(userService).saveUser(sender);
        verify(userService).saveUser(receiver);
    }

    @Test
    @DisplayName("createTransfer: deve lançar ReceiverUserNotFoundException quando receptor não existe")
    void createTransfer_receptorNaoExiste_lancaReceiverUserNotFoundException() {
        TransactionTransferDTO dto = new TransactionTransferDTO(new BigDecimal("300.00"), 99L);

        when(validatorService.validateReceiver(99L)).thenThrow(new ReceiverUserNotFoundException());

        assertThatThrownBy(() -> transactionService.createTransfer(sender, dto))
                .isInstanceOf(ReceiverUserNotFoundException.class);

        verify(validatorService).validateReceiver(99L);
        verify(validatorService, never()).validateTransfer(any(), any());
        verify(executeService, never()).executeTransfer(any(), any(), any());
        verify(transactionRepository, never()).save(any());
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("createTransfer: deve lançar InsufficientBalanceException quando saldo é insuficiente")
    void createTransfer_saldoInsuficiente_lancaInsufficientBalanceException() {
        TransactionTransferDTO dto = new TransactionTransferDTO(new BigDecimal("9999.00"), 2L);

        when(validatorService.validateReceiver(2L)).thenReturn(receiver);
        doThrow(new InsufficientBalanceException())
                .when(validatorService).validateTransfer(sender, new BigDecimal("9999.00"));

        assertThatThrownBy(() -> transactionService.createTransfer(sender, dto))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(validatorService).validateReceiver(2L);
        verify(validatorService).validateTransfer(sender, new BigDecimal("9999.00"));
        verify(executeService, never()).executeTransfer(any(), any(), any());
        verify(transactionRepository, never()).save(any());
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("createDeposit: deve criar transação de depósito e salvar quando dados são válidos")
    void createDeposit_dadosValidos_criaTransacaoESalva() {
        TransactionDepositDTO dto = new TransactionDepositDTO(new BigDecimal("200.00"));

        doNothing().when(executeService).executeDeposit(sender, new BigDecimal("200.00"));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.createDeposit(sender, dto);

        assertThat(result.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(result.getSender()).isEqualTo(sender);
        assertThat(result.getReceiver()).isNull();

        verify(executeService).executeDeposit(sender, new BigDecimal("200.00"));
        verify(transactionRepository).save(any(Transaction.class));
        verify(userService).saveUser(sender);
    }

    @Test
    @DisplayName("createDeposit: não deve chamar validatorService pois depósito não requer validação de saldo")
    void createDeposit_dadosValidos_naoValidaSaldo() {
        TransactionDepositDTO dto = new TransactionDepositDTO(new BigDecimal("200.00"));

        doNothing().when(executeService).executeDeposit(sender, new BigDecimal("200.00"));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        transactionService.createDeposit(sender, dto);

        verifyNoInteractions(validatorService);
    }

    @Test
    @DisplayName("createWithdraw: deve criar transação de saque e salvar quando dados são válidos")
    void createWithdraw_dadosValidos_criaTransacaoESalva() {
        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(new BigDecimal("300.00"));

        doNothing().when(validatorService).validateWithdraw(sender, new BigDecimal("300.00"));
        doNothing().when(executeService).executeWithdraw(sender, new BigDecimal("300.00"));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.createWithdraw(sender, dto);

        assertThat(result.getType()).isEqualTo(TransactionType.WITHDRAW);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(result.getSender()).isEqualTo(sender);
        assertThat(result.getReceiver()).isNull();

        verify(validatorService).validateWithdraw(sender, new BigDecimal("300.00"));
        verify(executeService).executeWithdraw(sender, new BigDecimal("300.00"));
        verify(transactionRepository).save(any(Transaction.class));
        verify(userService).saveUser(sender);
    }

    @Test
    @DisplayName("createWithdraw: deve lançar InsufficientBalanceException quando saldo é insuficiente")
    void createWithdraw_saldoInsuficiente_lancaInsufficientBalanceException() {
        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(new BigDecimal("9999.00"));

        doThrow(new InsufficientBalanceException())
                .when(validatorService).validateWithdraw(sender, new BigDecimal("9999.00"));

        assertThatThrownBy(() -> transactionService.createWithdraw(sender, dto))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(validatorService).validateWithdraw(sender, new BigDecimal("9999.00"));
        verify(executeService, never()).executeWithdraw(any(), any());
        verify(transactionRepository, never()).save(any());
        verifyNoInteractions(userService);
    }
}