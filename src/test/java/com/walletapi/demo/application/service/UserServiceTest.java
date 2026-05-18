package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.UserRegisterDTO;
import com.walletapi.demo.application.dto.UserResponseDTO;
import com.walletapi.demo.application.dto.UserUpdateDTO;
import com.walletapi.demo.application.dto.ViaCepResponseDTO;
import com.walletapi.demo.application.exceptions.ReceiverUserNotFoundException;
import com.walletapi.demo.application.exceptions.UnauthorizedUserAccessException;
import com.walletapi.demo.application.exceptions.UserNotFoundException;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.UserCredentials;
import com.walletapi.demo.domain.entities.Wallet;
import com.walletapi.demo.domain.enums.UserRole;
import com.walletapi.demo.domain.enums.WalletStatus;
import com.walletapi.demo.infrastructure.repositories.UserCredentialsRepository;
import com.walletapi.demo.infrastructure.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCredentialsRepository userCredentialsRepository;

    @Mock
    private ViaCepService viaCepService;

    private User user;
    private UserCredentials userCredentials;
    private UserCredentials adminCredentials;

    @BeforeEach
    void setUp() {
        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal("500.00"));
        wallet.setStatus(WalletStatus.ACTIVE);

        user = new User();
        user.setId(1L);
        user.setName("Gabriel");
        user.setEmail("gabriel@email.com");
        user.setDocument("123.456.789-00");
        user.setPhone("(44) 99999-9999");
        user.setBirthDate(LocalDate.of(2001, 1, 1));
        user.setWallet(wallet);

        userCredentials = new UserCredentials();
        userCredentials.setId("uuid-user");
        userCredentials.setLogin("gabriel@email.com");
        userCredentials.setPassword("hash");
        userCredentials.setRole(UserRole.USER);
        userCredentials.setUser(user);

        User adminUser = new User();
        adminUser.setId(99L);

        adminCredentials = new UserCredentials();
        adminCredentials.setId("uuid-admin");
        adminCredentials.setLogin("admin@email.com");
        adminCredentials.setPassword("hash");
        adminCredentials.setRole(UserRole.ADMIN);
        adminCredentials.setUser(adminUser);
    }

    @Test
    @DisplayName("findUserById: deve retornar o usuário quando o id existe")
    void findUserById_idExiste_retornaUsuario() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findUserById(1L);

        assertThat(result).isEqualTo(user);
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("findUserById: deve lançar UserNotFoundException quando o id não existe")
    void findUserById_idNaoExiste_lancaUserNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(99L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(99L);
    }

    @Test
    @DisplayName("findReceiverById: deve retornar o usuário quando o id existe")
    void findReceiverById_idExiste_retornaUsuario() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findReceiverById(1L);

        assertThat(result).isEqualTo(user);
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("findReceiverById: deve lançar ReceiverUserNotFoundException quando o id não existe")
    void findReceiverById_idNaoExiste_lancaReceiverUserNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findReceiverById(99L))
                .isInstanceOf(ReceiverUserNotFoundException.class);

        verify(userRepository).findById(99L);
    }

    @Test
    @DisplayName("getAllUsers: deve retornar lista de DTOs de todos os usuários")
    void getAllUsers_usuariosExistem_retornaLista() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponseDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Gabriel");
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("getAllUsers: deve retornar lista vazia quando não há usuários")
    void getAllUsers_semUsuarios_retornaListaVazia() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponseDTO> result = userService.getAllUsers();

        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("createUser: deve salvar usuário e credenciais quando dados são válidos")
    void createUser_dadosValidos_salvaUsuarioECredenciais() {
        UserRegisterDTO dto = new UserRegisterDTO("novo@email.com", "Senha@123", UserRole.USER);

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userCredentialsRepository.save(any(UserCredentials.class))).thenReturn(userCredentials);

        userService.createUser(dto);

        verify(userRepository).save(any(User.class));
        verify(userCredentialsRepository).save(any(UserCredentials.class));
    }

    @Test
    @DisplayName("updateUser: deve atualizar campos simples quando usuário é dono da conta")
    void updateUser_proprietarioAtualizaCamposSimples_retornaUsuarioAtualizado() {
        UserUpdateDTO dto = new UserUpdateDTO(
                "Gabriel Novo", null, null,
                "(44) 98888-8888", null, null, null,
                null, "123.456.789-00"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUser(1L, dto, userCredentials);

        assertThat(result.getName()).isEqualTo("Gabriel Novo");
        assertThat(result.getPhone()).isEqualTo("(44) 98888-8888");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser: deve atualizar endereço quando CEP é informado")
    void updateUser_cepInformado_atualizaEndereco() {
        UserUpdateDTO dto = new UserUpdateDTO(
                null, null, null,
                null, "87020-900", "123", null,
                null, "123.456.789-00"
        );

        ViaCepResponseDTO viaCep = new ViaCepResponseDTO(
                "87020-900", "Av. Brasil", "", "Centro", "Maringá", "PR"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(viaCepService.buscarEnderecoPorCep("87020-900")).thenReturn(viaCep);
        when(viaCepService.montarEnderecoCompleto(any(), any(), any())).thenReturn("Av. Brasil, 123 - Centro, Maringá - PR");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updateUser(1L, dto, userCredentials);

        verify(viaCepService).buscarEnderecoPorCep("87020-900");
        verify(viaCepService).montarEnderecoCompleto(any(), any(), any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser: deve lançar UnauthorizedUserAccessException quando USER tenta atualizar conta de outro")
    void updateUser_userTentaAtualizarOutro_lancaUnauthorizedUserAccessException() {
        UserUpdateDTO dto = new UserUpdateDTO(
                "Hacker", null, null,
                null, null, null, null,
                null, "123.456.789-00"
        );

        assertThatThrownBy(() -> userService.updateUser(2L, dto, userCredentials))
                .isInstanceOf(UnauthorizedUserAccessException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateUser: deve permitir ADMIN atualizar conta de outro usuário")
    void updateUser_adminAtualizaOutro_atualizaComSucesso() {
        UserUpdateDTO dto = new UserUpdateDTO(
                "Nome Novo", null, null,
                null, null, null, null,
                null, "123.456.789-00"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // adminCredentials aponta para adminUser.id = 99, atualizando user.id = 1
        User result = userService.updateUser(1L, dto, adminCredentials);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser: deve lançar UserNotFoundException quando usuário não existe")
    void updateUser_usuarioNaoExiste_lancaUserNotFoundException() {
        UserUpdateDTO dto = new UserUpdateDTO(
                "Gabriel", null, null,
                null, null, null, null,
                null, "123.456.789-00"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(1L, dto, userCredentials))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteUser: deve deletar usuário quando é o próprio dono da conta")
    void deleteUser_proprietarioDeletaPropraConta_deletaComSucesso() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L, userCredentials);

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("deleteUser: deve permitir ADMIN deletar conta de outro usuário")
    void deleteUser_adminDeletaOutro_deletaComSucesso() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L, adminCredentials);

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("deleteUser: deve lançar UnauthorizedUserAccessException quando USER tenta deletar conta de outro")
    void deleteUser_userTentaDeletarOutro_lancaUnauthorizedUserAccessException() {
        assertThatThrownBy(() -> userService.deleteUser(2L, userCredentials))
                .isInstanceOf(UnauthorizedUserAccessException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("deleteUser: deve lançar UserNotFoundException quando usuário não existe")
    void deleteUser_usuarioNaoExiste_lancaUserNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L, adminCredentials))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).delete(any());
    }
}