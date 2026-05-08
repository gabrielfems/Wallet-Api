package com.walletapi.demo.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walletapi.demo.application.dto.UserCreateDTO;
import com.walletapi.demo.application.dto.UserResponseDTO;
import com.walletapi.demo.application.dto.UserUpdateDTO;
import com.walletapi.demo.application.exceptions.CepNotFoundException;
import com.walletapi.demo.application.exceptions.UserNotFoundException;
import com.walletapi.demo.application.service.UserService;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.Wallet;
import com.walletapi.demo.domain.enums.WalletStatus;
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
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        user = new User();
        wallet = new Wallet();
        wallet.setBalance(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should return 201 when user created successfully")
    void createUserCase1() throws Exception {
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
                LocalDate.of(2001, 5, 20), null);

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
                LocalDate.of(2001, 5, 20), "123");

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
                LocalDate.of(2001, 5, 20), "000.000.000-00");

        when(userService.createUser(dto)).thenThrow(new CepNotFoundException("00000-000"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(userService).createUser(dto);
    }

    @Test
    @DisplayName("Should return 200 and list of users when users exist")
    void getAllUsersCase1() throws Exception {
        UserResponseDTO response = new UserResponseDTO(1L, "nomeValido", "email@valido.com",
                "000.000.000-00", "(00) 00000-0000", "Rua Valida",
                LocalDate.of(2001, 1, 1), BigDecimal.valueOf(1000), WalletStatus.ACTIVE);

        when(userService.getAllUsers()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/users/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("nomeValido"))
                .andExpect(jsonPath("$[0].email").value("email@valido.com"));

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("Should return 200 when user is updated successfully")
    void updateUserCase1() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO("nomeAtualizado", null, null, null, null, null, null, null);

        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.valueOf(1000));
        wallet.setStatus(WalletStatus.ACTIVE);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("nomeAtualizado");
        updatedUser.setEmail("email@valido.com");
        updatedUser.setDocument("000.000.000-00");
        updatedUser.setPhone("(00) 00000-0000");
        updatedUser.setCep("00000-000");
        updatedUser.setBirthDate(LocalDate.of(1990, 1, 1));
        updatedUser.setWallet(wallet);

        when(userService.updateUser(1L, dto)).thenReturn(updatedUser);

        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("nomeAtualizado"));

        verify(userService).updateUser(1L, dto);
    }

    @Test
    @DisplayName("Should return 400 when name is too short")
    void updateUserCase2() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO("ab", null, null, null, null, null, null, null);

        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 when email is invalid")
    void updateUserCase3() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO(null, "emailinvalido", null, null, null, null, null, null);

        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 when password is weak")
    void updateUserCase4() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO(null, null, "senhafraca", null, null, null, null, null);

        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 when phone is invalid")
    void updateUserCase5() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO(null, null, null, "999999999", null, null, null, null);

        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 400 when CEP format is invalid")
    void updateUserCase6() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO(null, null, null, null, "cep-errado", null, null, null);

        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return 404 when user is not found")
    void updateUserCase7() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO("nomeValido", null, null, null, null, null, null, null);

        when(userService.updateUser(1L, dto)).thenThrow(new UserNotFoundException(1L));

        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(userService).updateUser(1L, dto);
    }

    @Test
    @DisplayName("Should return 404 when CEP is not found")
    void updateUserCase8() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO(null, null, null, null, "00000-000", null, null, null);

        when(userService.updateUser(1L, dto)).thenThrow(new CepNotFoundException("00000-000"));

        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(userService).updateUser(1L, dto);
    }

    @Test
    @DisplayName("Should return 204 when user deleted successfully")
    void deleteUserCase1() throws Exception{
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void deleteUserCase2() throws Exception{
        doThrow(new UserNotFoundException(1L)).when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(1L);
    }
}