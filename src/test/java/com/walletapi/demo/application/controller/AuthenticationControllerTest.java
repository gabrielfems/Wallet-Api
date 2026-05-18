package com.walletapi.demo.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walletapi.demo.application.dto.AuthenticationDTO;
import com.walletapi.demo.application.dto.UserRegisterDTO;
import com.walletapi.demo.application.service.TokenService;
import com.walletapi.demo.application.service.UserService;
import com.walletapi.demo.domain.entities.UserCredentials;
import com.walletapi.demo.domain.enums.UserRole;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfigurations.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserCredentialsRepository userCredentialsRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private TokenService tokenService;

    private UserCredentials credentials;

    @BeforeEach
    void setUp() {
        credentials = new UserCredentials();
        credentials.setId("uuid-teste");
        credentials.setLogin("gabriel@email.com");
        credentials.setPassword("hash");
        credentials.setRole(UserRole.USER);
    }

    @Test
    @DisplayName("login: deve retornar 200 e o token JWT quando credenciais são válidas")
    void login_credenciaisValidas_retorna200() throws Exception {
        AuthenticationDTO dto = new AuthenticationDTO("gabriel@email.com", "Senha@123");

        var authToken = new UsernamePasswordAuthenticationToken(credentials, null, credentials.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authToken);
        when(tokenService.generateToken(credentials)).thenReturn("jwt-token-gerado");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-gerado"));

        verify(authenticationManager).authenticate(any());
        verify(tokenService).generateToken(any(UserCredentials.class));
    }

    @Test
    @DisplayName("login: deve retornar 400 quando email tem formato inválido")
    void login_emailInvalido_retorna400() throws Exception {
        AuthenticationDTO dto = new AuthenticationDTO("email-invalido", "Senha@123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("login: deve retornar 400 quando senha não atende aos requisitos")
    void login_senhaFraca_retorna400() throws Exception {
        AuthenticationDTO dto = new AuthenticationDTO("gabriel@email.com", "fraca");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("login: deve retornar 403 quando credenciais são inválidas")
    void login_credenciaisInvalidas_retorna403() throws Exception {
        AuthenticationDTO dto = new AuthenticationDTO("gabriel@email.com", "Senha@123");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(authenticationManager).authenticate(any());
        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("register: deve retornar 200 quando dados são válidos e login não existe")
    void register_dadosValidos_retorna200() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO("gabriel@email.com", "Senha@123", UserRole.USER);

        when(userCredentialsRepository.findByLogin("gabriel@email.com")).thenReturn(null);
        doNothing().when(userService).createUser(any(UserRegisterDTO.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(userService).createUser(any(UserRegisterDTO.class));
    }

    @Test
    @DisplayName("register: deve retornar 400 quando login já está cadastrado")
    void register_loginJaExiste_retorna400() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO("gabriel@email.com", "Senha@123", UserRole.USER);

        when(userCredentialsRepository.findByLogin("gabriel@email.com")).thenReturn(credentials);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("register: deve retornar 400 quando email tem formato inválido")
    void register_emailInvalido_retorna400() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO("email-invalido", "Senha@123", UserRole.USER);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("register: deve retornar 400 quando senha não atende aos requisitos")
    void register_senhaFraca_retorna400() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO("gabriel@email.com", "fraca", UserRole.USER);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }
}