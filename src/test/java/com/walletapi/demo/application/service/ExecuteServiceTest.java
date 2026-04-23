package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.TransactionDepositDTO;
import com.walletapi.demo.application.dto.TransactionWithdrawDTO;
import com.walletapi.demo.application.dto.UserCreateDTO;
import com.walletapi.demo.application.exceptions.InsufficientBalanceException;
import com.walletapi.demo.domain.entities.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

class ExecuteServiceTest {

    private ExecuteService executeService;

    private Validator validator;

    @InjectMocks
    private TransactionValidatorService transactionValidatorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        executeService = new ExecuteService();
    }

    @Test
    @DisplayName("Should execute a transfer when everything is OK")
    void executeTransferCase1() {
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
    @DisplayName("Should execute the deposit when everything is OK")
    void executeDepositCase1() {
        UserCreateDTO sender = new UserCreateDTO("Gabriel",
                "gabriel.fe132@gmail.com",
                "123@pass",
                "44974002293",
                "87080283",
                "123",
                "b",
                LocalDate.of(2002, 02, 24),
                "1234567890");

        User createUser = new User(sender);
        createUser.setId(1L);
        createUser.getWallet().setBalance(BigDecimal.valueOf(100));

        executeService.executeDeposit(createUser, BigDecimal.valueOf(100));

        assertThat(createUser.getWallet().getBalance())
        .isEqualByComparingTo("200");

    }

    @Test
    @DisplayName("Should thrown a exception when amount is NULL")
    void executeDepositCase2() {
        TransactionDepositDTO dto = new TransactionDepositDTO(null, 1L);

        Set<ConstraintViolation<TransactionDepositDTO>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                )
                .containsExactly(
                        tuple("amount", "Valor obrigatório")
                );
    }

    @Test
    @DisplayName("Should thrown a exception when amount is NEGATIVE")
    void executeDepositCase3() {
        TransactionDepositDTO dto = new TransactionDepositDTO(BigDecimal.valueOf(-1), 1L);

        Set<ConstraintViolation<TransactionDepositDTO>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                )
                .containsExactly(
                        tuple("amount", "Valor deve ser positivo")
                );
    }

    @Test
    @DisplayName("Should thrown a exception when user is EMPTY")
    void executeDepositCase4() {
        TransactionDepositDTO dto = new TransactionDepositDTO(BigDecimal.valueOf(10), null);

        Set<ConstraintViolation<TransactionDepositDTO>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                )
                .containsExactly(
                        tuple("userId", "Usuário obrigatório")
                );
    }

    @Test
    @DisplayName("Should execute the withdraw when everything is OK")
    void executeWithdrawCase1() {
        UserCreateDTO sender = new UserCreateDTO("Gabriel",
                "gabriel@gmail.com",
                "123@pass",
                "44974002293",
                "87080078",
                "123",
                "b",
                LocalDate.of(2002, 12, 20),
                "1234567890");

        User createUser = new User(sender);
        createUser.setId(1L);
        createUser.getWallet().setBalance(BigDecimal.valueOf(100));

        executeService.executeWithdraw(createUser, BigDecimal.valueOf(100));

        assertThat(createUser.getWallet().getBalance())
                .isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Should thrown a exception when amount is NULL")
    void executeWithdrawCase2() {
        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(null, 1L);

        Set<ConstraintViolation<TransactionWithdrawDTO>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                )
                .containsExactly(
                        tuple("amount", "Valor obrigatório")
                );
    }

    @Test
    @DisplayName("Should thrown a exception when amount is NEGATIVE")
    void executeWithdrawCase3() {
        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(BigDecimal.valueOf(-1), 1L);

        Set<ConstraintViolation<TransactionWithdrawDTO>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                )
                .containsExactly(
                        tuple("amount", "Valor deve ser positivo")
                );
    }

    @Test
    @DisplayName("Should thrown a exception when user is EMPTY")
    void executeWithdrawCase4() {
        TransactionWithdrawDTO dto = new TransactionWithdrawDTO(BigDecimal.valueOf(10), null);

        Set<ConstraintViolation<TransactionWithdrawDTO>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                )
                .containsExactly(
                        tuple("senderId", "Remetente obrigatório")
                );
    }

}