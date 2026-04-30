package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.GoalBoxCreateDTO;
import com.walletapi.demo.application.exceptions.GoalBoxNotFoundException;
import com.walletapi.demo.application.exceptions.UnauthorizedBoxAccessException;
import com.walletapi.demo.application.exceptions.UserNotFoundException;
import com.walletapi.demo.domain.entities.GoalBox;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.infrastructure.repositories.GoalBoxRepository;
import com.walletapi.demo.infrastructure.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

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

    @Mock
    UserRepository userRepository;

    GoalBox goalBox;
    User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        goalBox = new GoalBox();
        goalBox.setName("any");
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
    void getUserBoxes() {
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
    void deposit() {
    }

    @Test
    void deleteBox() {
    }

    @Test
    void withdraw() {
    }

    @Test
    void updateBox() {
    }
}