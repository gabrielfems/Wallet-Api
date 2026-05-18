package com.walletapi.demo.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.walletapi.demo.application.dto.UserUpdateDTO;
import com.walletapi.demo.application.exceptions.UnauthorizedUserAccessException;
import com.walletapi.demo.application.exceptions.UserNotFoundException;
import com.walletapi.demo.application.service.TokenService;
import com.walletapi.demo.application.service.UserService;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.UserCredentials;
import com.walletapi.demo.domain.entities.Wallet;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfigurations.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserCredentialsRepository userCredentialsRepository;

    private static final String FAKE_TOKEN       = "Bearer fake-jwt-token";
    private static final String FAKE_ADMIN_TOKEN = "Bearer fake-jwt-admin-token";

    private User user;
    private UserCredentials userCredentials;
    private UserCredentials adminCredentials;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

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
        adminUser.setWallet(wallet);

        adminCredentials = new UserCredentials();
        adminCredentials.setId("uuid-admin");
        adminCredentials.setLogin("admin@email.com");
        adminCredentials.setPassword("hash");
        adminCredentials.setRole(UserRole.ADMIN);
        adminCredentials.setUser(adminUser);

        when(tokenService.validateToken("fake-jwt-token")).thenReturn("gabriel@email.com");
        when(userCredentialsRepository.findByLogin("gabriel@email.com")).thenReturn(userCredentials);

        when(tokenService.validateToken("fake-jwt-admin-token")).thenReturn("admin@email.com");
        when(userCredentialsRepository.findByLogin("admin@email.com")).thenReturn(adminCredentials);
    }

    @Test
    @DisplayName("getAllUsers: deve retornar 200 quando autenticado como ADMIN")
    void getAllUsers_comAdmin_retorna200() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users/list")
                        .header("Authorization", FAKE_ADMIN_TOKEN))
                .andExpect(status().isOk());

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("getAllUsers: deve retornar 403 quando autenticado como USER comum")
    void getAllUsers_comUserComum_retorna403() throws Exception {
        mockMvc.perform(get("/api/users/list")
                        .header("Authorization", FAKE_TOKEN))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("getAllUsers: deve retornar 403 quando não autenticado")
    void getAllUsers_semToken_retorna403() throws Exception {
        mockMvc.perform(get("/api/users/list"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("updateUser: deve retornar 200 quando usuário atualiza sua própria conta")
    void updateUser_propriaConta_retorna200() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO(
                "Gabriel Novo", null, null,
                "(44) 98888-8888", null, null, null,
                null, "123.456.789-00"
        );

        when(userService.updateUser(eq(1L), any(UserUpdateDTO.class), any(UserCredentials.class)))
                .thenReturn(user);

        mockMvc.perform(patch("/api/users/1")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(userService).updateUser(eq(1L), any(UserUpdateDTO.class), any(UserCredentials.class));
    }

    @Test
    @DisplayName("updateUser: deve retornar 200 quando ADMIN atualiza conta de outro usuário")
    void updateUser_adminAtualizaOutro_retorna200() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO(
                "Nome Atualizado", null, null,
                null, null, null, null,
                null, "123.456.789-00"
        );

        when(userService.updateUser(eq(1L), any(UserUpdateDTO.class), any(UserCredentials.class)))
                .thenReturn(user);

        mockMvc.perform(patch("/api/users/1")
                        .header("Authorization", FAKE_ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(userService).updateUser(eq(1L), any(UserUpdateDTO.class), any(UserCredentials.class));
    }

    @Test
    @DisplayName("updateUser: deve retornar 403 quando USER tenta atualizar conta de outro usuário")
    void updateUser_contaDeOutro_retorna403() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO(
                "Hacker", null, null,
                null, null, null, null,
                null, "123.456.789-00"
        );

        when(userService.updateUser(eq(2L), any(UserUpdateDTO.class), any(UserCredentials.class)))
                .thenThrow(new UnauthorizedUserAccessException());

        mockMvc.perform(patch("/api/users/2")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(userService).updateUser(eq(2L), any(UserUpdateDTO.class), any(UserCredentials.class));
    }

    @Test
    @DisplayName("updateUser: deve retornar 400 quando document está em branco")
    void updateUser_documentEmBranco_retorna400() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO(
                "Gabriel", null, null,
                null, null, null, null,
                null, ""
        );

        mockMvc.perform(patch("/api/users/1")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("updateUser: deve retornar 400 quando phone tem formato inválido")
    void updateUser_phoneInvalido_retorna400() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO(
                null, null, null,
                "telefone-invalido", null, null, null,
                null, "123.456.789-00"
        );

        mockMvc.perform(patch("/api/users/1")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("updateUser: deve retornar 400 quando birthDate é no futuro")
    void updateUser_birthDateFuturo_retorna400() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO(
                null, null, null,
                null, null, null, null,
                LocalDate.now().plusYears(1), "123.456.789-00"
        );

        mockMvc.perform(patch("/api/users/1")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("updateUser: deve retornar 404 quando usuário não existe")
    void updateUser_usuarioNaoEncontrado_retorna404() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO(
                "Gabriel", null, null,
                null, null, null, null,
                null, "123.456.789-00"
        );

        when(userService.updateUser(eq(1L), any(UserUpdateDTO.class), any(UserCredentials.class)))
                .thenThrow(new UserNotFoundException(1L));

        mockMvc.perform(patch("/api/users/1")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(userService).updateUser(eq(1L), any(UserUpdateDTO.class), any(UserCredentials.class));
    }

    @Test
    @DisplayName("deleteUser: deve retornar 204 quando usuário deleta sua própria conta")
    void deleteUser_propriaConta_retorna204() throws Exception {
        doNothing().when(userService).deleteUser(eq(1L), any(UserCredentials.class));

        mockMvc.perform(delete("/api/users/1")
                        .header("Authorization", FAKE_TOKEN))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(eq(1L), any(UserCredentials.class));
    }

    @Test
    @DisplayName("deleteUser: deve retornar 204 quando ADMIN deleta conta de outro usuário")
    void deleteUser_adminDeletaOutro_retorna204() throws Exception {
        doNothing().when(userService).deleteUser(eq(1L), any(UserCredentials.class));

        mockMvc.perform(delete("/api/users/1")
                        .header("Authorization", FAKE_ADMIN_TOKEN))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(eq(1L), any(UserCredentials.class));
    }

    @Test
    @DisplayName("deleteUser: deve retornar 403 quando USER tenta deletar conta de outro usuário")
    void deleteUser_contaDeOutro_retorna403() throws Exception {
        doThrow(new UnauthorizedUserAccessException())
                .when(userService).deleteUser(eq(2L), any(UserCredentials.class));

        mockMvc.perform(delete("/api/users/2")
                        .header("Authorization", FAKE_TOKEN))
                .andExpect(status().isForbidden());

        verify(userService).deleteUser(eq(2L), any(UserCredentials.class));
    }

    @Test
    @DisplayName("deleteUser: deve retornar 404 quando usuário não existe")
    void deleteUser_usuarioNaoEncontrado_retorna404() throws Exception {
        doThrow(new UserNotFoundException(1L))
                .when(userService).deleteUser(eq(1L), any(UserCredentials.class));

        mockMvc.perform(delete("/api/users/1")
                        .header("Authorization", FAKE_TOKEN))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(eq(1L), any(UserCredentials.class));
    }
}