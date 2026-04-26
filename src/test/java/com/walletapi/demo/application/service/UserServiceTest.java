package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.UserCreateDTO;
import com.walletapi.demo.application.dto.ViaCepResponseDTO;
import com.walletapi.demo.application.exceptions.ReceiverUserNotFoundException;
import com.walletapi.demo.application.exceptions.SenderUserNotFoundException;
import com.walletapi.demo.application.exceptions.UserNotFoundException;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.infrastructure.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private User user;

    @InjectMocks
    UserService userService;

    @Mock
    ViaCepService viaCepService;

    @Mock
    UserRepository userRepository;

    private UserCreateDTO dto1;

    @BeforeEach
    void setUp() {
        UserCreateDTO dto1 = new UserCreateDTO(
                "Gabriel", "gabriel@gmail.com", "123@pass",
                "44974002293", "87080078", "123", "b",
                LocalDate.of(2002, 12, 20), "1234567890");

        user = new User(dto1);
        user.setId(1L);
    }

    @Test
    @DisplayName("Should return a User because ID is present on db")
    void findUserByIdCase1() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(userRepository, times(1)).findById(1L);

    }

    @Test
    @DisplayName("Should thrown exception because user ID does not exists")
    void findUserByIdCase2() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(1L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findById(1L);

    }

    @Test
    @DisplayName("Should return a valid Sender when ID is present on db")
    void findSenderByIdCase1() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findSenderById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should thrown exception because sender ID does not exists")
    void findSenderByIdCase2() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findSenderById(1L))
                .isInstanceOf(SenderUserNotFoundException.class);

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return a valid Receiver when ID is present on db")
    void findReceiverByIdCase1() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findReceiverById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should thrown exception because sender ID does not exists")
    void findReceiverByIdCase2() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findReceiverById(1L))
                .isInstanceOf(ReceiverUserNotFoundException.class);

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should save an User correctly on db")
    void saveUserCase1() {
        when(userRepository.save(user)).thenReturn(user);

        userService.saveUser(user);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should create correctly an User with all his datas")
    void createUserCase1() {

    }

    @Test
    void getAllUsers() {
    }

    @Test
    void updateUser() {
    }

    @Test
    void deleteUser() {
    }
}