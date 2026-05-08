package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.UserCreateDTO;
import com.walletapi.demo.application.dto.UserResponseDTO;
import com.walletapi.demo.application.dto.UserUpdateDTO;
import com.walletapi.demo.application.dto.ViaCepResponseDTO;
import com.walletapi.demo.application.exceptions.CepNotFoundException;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    User user;

    @InjectMocks
    UserService userService;

    @Mock
    ViaCepService viaCepService;

    @Mock
    UserRepository userRepository;

    UserCreateDTO dtoValido;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        dtoValido = new UserCreateDTO(
                "Gabriel", "gabriel@gmail.com", "123@pass",
                "44974002293", "87080078", "123", "b",
                LocalDate.of(2002, 12, 20), "1234567890");

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

        ViaCepResponseDTO viaCepResponse = new ViaCepResponseDTO(
                "87080-078", "Rua das Flores", "", "Jardim Alvorada", "Maringá", "PR"
        );

        String enderecoCompleto = "Rua das Flores, 123 - b, Jardim Alvorada, Maringá - PR, CEP: 87080-078";

        when(viaCepService.buscarEnderecoPorCep("87080078")).thenReturn(viaCepResponse);
        when(viaCepService.montarEnderecoCompleto(viaCepResponse, "123", "b")).thenReturn(enderecoCompleto);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser(dtoValido);

        assertThat(result).isNotNull();
        assertThat(result.getCep()).isEqualTo(enderecoCompleto);
        assertThat(result.getNumero()).isEqualTo("123");
        assertThat(result.getComplemento()).isEqualTo("b");

        verify(viaCepService, times(1)).buscarEnderecoPorCep("87080078");
        verify(viaCepService, times(1)).montarEnderecoCompleto(viaCepResponse, "123", "b");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw CepNotFoundException when CEP is invalid")
    void createUserCase2() {
        doThrow(new CepNotFoundException("00000000"))
                .when(viaCepService).buscarEnderecoPorCep("00000000");

        UserCreateDTO dtoInvalido = new UserCreateDTO(
                "Gabriel", "gabriel@gmail.com", "123@pass",
                "44974002293", "00000000", "123", "b",
                LocalDate.of(2002, 12, 20), "1234567890"
        );

        assertThatThrownBy(() -> userService.createUser(dtoInvalido))
                .isInstanceOf(CepNotFoundException.class);
    }

    @Test
    @DisplayName("Should return a list of users")
    void getAllUsersCase1() {
        User userList = new User(dtoValido);

        when(userRepository.findAll()).thenReturn(List.of(userList));

        List<UserResponseDTO> list = userService.getAllUsers();

        assertThat(list).isNotNull();
    }

    @Test
    @DisplayName("Should update user with new CEP")
    void updateUserCase1() {
        User user = new User(dtoValido);

        UserUpdateDTO dto = new UserUpdateDTO(null, null, null, null, "87080078", "456", null, null);

        ViaCepResponseDTO viaCepResponse = new ViaCepResponseDTO(
                "87080-078", "Rua das Flores", "", "Jardim Alvorada", "Maringá", "PR"
        );
        String enderecoCompleto = "Rua das Flores, 456, Jardim Alvorada, Maringá - PR, CEP: 87080-078";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(viaCepService.buscarEnderecoPorCep("87080078")).thenReturn(viaCepResponse);
        when(viaCepService.montarEnderecoCompleto(any(), any(), any())).thenReturn(enderecoCompleto);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUser(1L, dto);

        assertThat(result.getCep()).isEqualTo(enderecoCompleto);

        verify(viaCepService, times(1)).buscarEnderecoPorCep("87080078");
        verify(userRepository).save(result);
    }

    @Test
    @DisplayName("Should update user without changing address")
    void updateUserCase2() {
        User user = new User(dtoValido);

        UserUpdateDTO dto = new UserUpdateDTO("NovoNome", null, null, null, null, null, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUser(1L, dto);

        assertThat(result.getName()).isEqualTo("NovoNome");

        verify(userRepository).save(result);
    }

    @Test
    @DisplayName("Should throw exception when user is not found")
    void updateUserCase3() {
        UserUpdateDTO dto = new UserUpdateDTO(null, null, null, null, null, null, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(1L, dto))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete user when his id exists on db")
    void deleteUserCase1() {
        User userList = new User(dtoValido);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userList));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(userList);
    }

    @Test
    @DisplayName("Should throw exception because user id is not present")
    void deleteUserCase2() {
        doThrow(new UserNotFoundException(1L)).when(userRepository).findById(1L);

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(UserNotFoundException.class);

    }
}