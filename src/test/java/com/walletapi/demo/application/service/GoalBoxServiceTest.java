package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.GoalBoxCreateDTO;
import com.walletapi.demo.application.dto.GoalBoxResponseDTO;
import com.walletapi.demo.application.dto.GoalBoxUpdateDTO;
import com.walletapi.demo.application.exceptions.GoalBoxNotFoundException;
import com.walletapi.demo.application.exceptions.InsufficientBalanceException;
import com.walletapi.demo.application.exceptions.UnauthorizedBoxAccessException;
import com.walletapi.demo.application.exceptions.UserNotFoundException;
import com.walletapi.demo.domain.entities.GoalBox;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.Wallet;
import com.walletapi.demo.infrastructure.repositories.GoalBoxRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalBoxServiceTest {

    @InjectMocks
    GoalBoxService boxService;

    @Mock
    GoalBoxRepository boxRepository;

    @Mock
    UserService userService;

    GoalBox goalBox;
    User user;
    Wallet wallet;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.valueOf(1000));

        user.setWallet(wallet);

        goalBox = new GoalBox();
        goalBox.setName("any");
        goalBox.setCurrentBalance(BigDecimal.valueOf(500));
        goalBox.setTargetAmount(BigDecimal.valueOf(1000));
        goalBox.setUser(user);

    }

    @Test
    @DisplayName("Should create a new box succefully")
    void createBoxCase1() {
        GoalBoxCreateDTO dto = new GoalBoxCreateDTO("name",
                "description", BigDecimal.valueOf(10));

        when(userService.findUserById(1L)).thenReturn(user);
        when(boxRepository.save(any(GoalBox.class))).thenReturn(goalBox);

        GoalBox result = boxService.createBox(1L, dto);

        assertNotNull(result);
        verify(userService).findUserById(1L);
        verify(boxRepository).save(any(GoalBox.class));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user id is not into db")
    void createBoxCase2() {
        GoalBoxCreateDTO dto = new GoalBoxCreateDTO("name", "description", BigDecimal.valueOf(10));

        doThrow(new UserNotFoundException(1L)).when(userService).findUserById(1L);

        assertThrows(UserNotFoundException.class, () -> boxService.createBox(1L, dto));

        verify(userService).findUserById(1L);
    }

    @Test
    @DisplayName("Should return the box list of user")
    void getUserBoxesCase1() {
        when(userService.findUserById(1L)).thenReturn(user);
        when(boxRepository.findByUserId(1L)).thenReturn(List.of(goalBox));

        List<GoalBoxResponseDTO> result = boxService.getUserBoxes(1L);

        assertThat(result.getFirst().progress()).isEqualTo("50%");

        verify(userService).findUserById(1L);
        verify(boxRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void getUserBoxesCase2() {
        doThrow(new UserNotFoundException(1L)).when(userService).findUserById(1L);

        assertThatThrownBy(() -> boxService.getUserBoxes(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Should return a box succefully")
    void getBoxCase1() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(goalBox));

        GoalBox box = boxService.getBox(1L, 1L);

        assertEquals(goalBox, box);
        verify(boxRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw GoalBoxNotFoundException when box id not found")
    void getBoxCase2() {
        when(boxRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(GoalBoxNotFoundException.class, () -> boxService.getBox(1L, 1L));

        verify(boxRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw UnauthorizedBoxAccessException when user does not own the box")
    void getBoxCase3() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(goalBox));

        assertThrows(UnauthorizedBoxAccessException.class, () -> boxService.getBox(99L, 1L));

        verify(boxRepository).findById(1L);
    }

    @Test
    @DisplayName("Should deposit cash to the box succefully")
    void depositCase1() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(goalBox));
        when(boxRepository.save(goalBox)).thenReturn(goalBox);

        GoalBox result = boxService.deposit(1L, 1L, BigDecimal.valueOf(1000));

        assertThat(result.getCurrentBalance()).isEqualTo(BigDecimal.valueOf(1500));
        assertThat(wallet.getBalance()).isEqualTo(BigDecimal.valueOf(0));

        verify(userService).saveUser(user);
        verify(boxRepository).save(goalBox);
    }

    @Test
    @DisplayName("Should throw InsufficientBalanceException when wallet balance is insufficient")
    void depositCase2() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(goalBox));

        assertThatThrownBy(() -> boxService.deposit(1L, 1L, BigDecimal.valueOf(1500)))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("Should throw GoalBoxNotFoundException when box does not exist")
    void depositCase3() {
        when(boxRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boxService.deposit(1L, 1L, BigDecimal.valueOf(100)))
                .isInstanceOf(GoalBoxNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw UnauthorizedBoxAccessException when user does not own the box")
    void depositCase4() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(goalBox));

        assertThatThrownBy(() -> boxService.deposit(99L, 1L, BigDecimal.valueOf(100)))
                .isInstanceOf(UnauthorizedBoxAccessException.class);
    }

    @Test
    @DisplayName("Should delete an box succefully")
    void deleteBoxCase1() {
        user.setWallet(wallet);
        user.getWallet().setBalance(BigDecimal.valueOf(1000));

        when(boxRepository.findById(1L)).thenReturn(Optional.of(goalBox));
        when(userService.findUserById(1L)).thenReturn(user);

        boxService.deleteBox(1L, 1L);

        assertThat(user.getWallet().getBalance())
                .isEqualByComparingTo(BigDecimal.valueOf(1500));

        verify(userService).saveUser(user);
        verify(boxRepository).findById(1L);
        verify(boxRepository).delete(goalBox);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException")
    void deleteBoxCase2() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(goalBox));
        doThrow(new UserNotFoundException(1L)).when(userService).findUserById(1L);

        assertThatThrownBy(() -> boxService.deleteBox(1L, 1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw GoalBoxNotFoundException")
    void deleteBoxCase3() {
        when(boxRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boxService.deleteBox(1L, 1L))
                .isInstanceOf(GoalBoxNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw UnauthorizedBoxAccessException when user does not own the box")
    void deleteBoxCase4() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(goalBox));

        assertThatThrownBy(() -> boxService.deleteBox(99L, 1L))
                .isInstanceOf(UnauthorizedBoxAccessException.class);
    }

    @Test
    @DisplayName("Should withdraw cash from the box succefully")
    void withdrawCase1() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(goalBox));
        when(boxRepository.save(goalBox)).thenReturn(goalBox);

        GoalBox result = boxService.withdraw(1L, 1L, BigDecimal.valueOf(500));

        assertThat(wallet.getBalance()).isEqualTo(BigDecimal.valueOf(1500));
        assertThat(result.getCurrentBalance()).isEqualTo(BigDecimal.valueOf(0));

        verify(userService).saveUser(user);
        verify(boxRepository).save(goalBox);
    }

    @Test
    @DisplayName("Should throw InsufficientBalanceException when box balance is insufficient")
    void withdrawCase2() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(goalBox));

        assertThatThrownBy(() -> boxService.withdraw(1L, 1L, BigDecimal.valueOf(1500)))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("Should throw GoalBoxNotFoundException when box does not exist")
    void withdrawCase3() {
        when(boxRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boxService.withdraw(1L, 1L, BigDecimal.valueOf(100)))
                .isInstanceOf(GoalBoxNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw UnauthorizedBoxAccessException when user does not own the box")
    void withdrawCase4() {
        when(boxRepository.findById(1L)).thenReturn(Optional.of(goalBox));

        assertThatThrownBy(() -> boxService.withdraw(99L, 1L, BigDecimal.valueOf(100)))
                .isInstanceOf(UnauthorizedBoxAccessException.class);
    }

    @Test
    @DisplayName("Should update box successfully")
    void updateBoxCase1() {
        GoalBoxUpdateDTO dto = new GoalBoxUpdateDTO("NovoNome", "NovaDesc", BigDecimal.valueOf(100));

        when(boxRepository.findById(1L)).thenReturn(Optional.of(goalBox));
        when(boxRepository.save(goalBox)).thenReturn(goalBox);

        GoalBox result = boxService.updateBox(1L, 1L, dto);

        Assertions.assertThat(result.getName()).isEqualTo("NovoNome");
        assertThat(result.getDescription()).isEqualTo("NovaDesc");
        assertThat(result.getTargetAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));

        verify(boxRepository).save(goalBox);
    }

    @Test
    @DisplayName("Should throw GoalBoxNotFoundException when box does not exist")
    void updateBoxCase2() {
        GoalBoxUpdateDTO dto = new GoalBoxUpdateDTO("NovoNome", null, null);

        when(boxRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boxService.updateBox(1L, 1L, dto))
                .isInstanceOf(GoalBoxNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw UnauthorizedBoxAccessException when user does not own the box")
    void updateBoxCase3() {
        GoalBoxUpdateDTO dto = new GoalBoxUpdateDTO("NovoNome", null, null);

        when(boxRepository.findById(1L)).thenReturn(Optional.of(goalBox));

        assertThatThrownBy(() -> boxService.updateBox(99L, 1L, dto))
                .isInstanceOf(UnauthorizedBoxAccessException.class);
    }
}