package com.walletapi.demo.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walletapi.demo.application.dto.UserCreateDTO;
import com.walletapi.demo.application.exceptions.CepNotFoundException;
import com.walletapi.demo.application.service.UserService;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet();
        wallet.setBalance(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should return 201 when user created successfully")
    void createUserCase1() throws Exception {
        user = new User();
        user.setName("nomeValido");
        user.setEmail("email@valido.com");
        user.setPassword("s3Nh@Valid421");
        user.setPhone("(00) 00000-0000");
        user.setCep("00000-000");
        user.setNumero("123");
        user.setBirthDate(LocalDate.of(2001, 1, 1));
        user.setDocument("000.000.000-00");

        user.setId(1L);
        user.setWallet(wallet);

        UserCreateDTO dto = new UserCreateDTO("NomeValido", "email@valido.com", "s3Nh@Valid421",
                "(00) 00000-0000", "00000-000", "123", "",
                LocalDate.of(2001, 1, 1), "000.000.000-00");

        when(userService.createUser(dto)).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("nomeValido"))
                .andExpect(jsonPath("$.email").value("email@valido.com"));

        verify(userService).createUser(dto);
    }

    @Test
    @DisplayName("Should return 400 when user name is shorter than 2 characters")
    void createUserCase2() throws Exception {

        UserCreateDTO dto = new UserCreateDTO("A", "email@valido.com", "s3Nh@Valid421",
                "(00) 00000-0000", "00000-000", "123", "",
                LocalDate.of(2001, 1, 1), "000.000.000-00");

        when(userService.createUser(dto)).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 when email is null")
    void createUserCase3() throws Exception {

        UserCreateDTO dto = new UserCreateDTO("nomeValido", null, "s3Nh@Valid421",
                "(00) 00000-0000", "00000-000", "123", "",
                LocalDate.of(2001, 1, 1), "000.000.000-00");

        when(userService.createUser(dto)).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 when email is invalid")
    void createUserCase4() throws Exception {

        UserCreateDTO dto = new UserCreateDTO("nomeValido", "emailInvalido.com", "s3Nh@Valid421",
                "(00) 00000-0000", "00000-000", "123", "",
                LocalDate.of(2001, 1, 1), "000.000.000-00");

        when(userService.createUser(dto)).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 when password is null")
    void createUserCase5() throws Exception {

        UserCreateDTO dto = new UserCreateDTO("nomeValido", "emailInvalido.com", null,
                "(00) 00000-0000", "00000-000", "123", "",
                LocalDate.of(2001, 1, 1), "000.000.000-00");

        when(userService.createUser(dto)).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 when password is weak")
    void createUserCase6() throws Exception {

        UserCreateDTO dto = new UserCreateDTO("nomeValido", "email@valido.com", "senhaFraca",
                "(00) 00000-0000", "00000-000", "123", "",
                LocalDate.of(2001, 1, 1), "000.000.000-00");

        when(userService.createUser(dto)).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 when phone is null")
    void createUserCase7() throws Exception {

        UserCreateDTO dto = new UserCreateDTO("nomeValido", "email@valido.com", "s3Nh@Valid421",
                null, "00000-000", "123", "",
                LocalDate.of(2001, 1, 1), "000.000.000-00");

        when(userService.createUser(dto)).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 when phone is invalid")
    void createUserCase8() throws Exception {

        UserCreateDTO dto = new UserCreateDTO("nomeValido", "email@valido.com", "s3Nh@Valid421",
                "123", "00000-000", "123", "",
                LocalDate.of(2001, 1, 1), "000.000.000-00");

        when(userService.createUser(dto)).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 when CEP is null")
    void createUserCase9() throws Exception {

        UserCreateDTO dto = new UserCreateDTO("nomeValido", "email@valido.com", "s3Nh@Valid421",
                "(00) 00000-0000", null, "123", "",
                LocalDate.of(2001, 1, 1), "000.000.000-00");

        when(userService.createUser(dto)).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 when CEP is invalid")
    void createUserCase10() throws Exception {

        UserCreateDTO dto = new UserCreateDTO("nomeValido", "email@valido.com", "s3Nh@Valid421",
                "(00) 00000-0000", "123", "123", "",
                LocalDate.of(2001, 1, 1), "000.000.000-00");

        when(userService.createUser(dto)).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 when num. is null")
    void createUserCase11() throws Exception {

        UserCreateDTO dto = new UserCreateDTO("nomeValido", "email@valido.com", "s3Nh@Valid421",
                "(00) 00000-0000", "00000-000", null, "",
                LocalDate.of(2001, 1, 1), "000.000.000-00");

        when(userService.createUser(dto)).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 when bithDate is null")
    void createUserCase12() throws Exception {

        UserCreateDTO dto = new UserCreateDTO("nomeValido", "email@valido.com", "s3Nh@Valid421",
                "(00) 00000-0000", "00000-000", "123", "",
                null, "000.000.000-00");

        when(userService.createUser(dto)).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 when document is null")
    void createUserCase13() throws Exception {

        UserCreateDTO dto = new UserCreateDTO("nomeValido", "email@valido.com", "s3Nh@Valid421",
                "(00) 00000-0000", "00000-000", "123", "",
                LocalDate.of(2096, 5, 20), null);

        when(userService.createUser(dto)).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 when document is invalid")
    void createUserCase14() throws Exception {

        UserCreateDTO dto = new UserCreateDTO("nomeValido", "email@valido.com", "s3Nh@Valid421",
                "(00) 00000-0000", "00000-000", "123", "",
                LocalDate.of(2096, 5, 20), "123");

        when(userService.createUser(dto)).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 404 when CEP not found")
    void createUserCase15() throws Exception {
        UserCreateDTO dto = new UserCreateDTO("nomeValido", "email@valido.com", "s3Nh@Valid421",
                "(00) 00000-0000", "00000-000", "123", "",
                LocalDate.of(2096, 5, 20), "000.000.000-00");

        when(userService.createUser(dto)).thenThrow(new CepNotFoundException("00000-000"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(userService).createUser(dto);
    }

    @Test
    @DisplayName("Should return 200 when ")
    void getAllUsersCase1() {

    }

    @Test
    void updateUser() {
    }

    @Test
    void deleteUser() {
    }
}