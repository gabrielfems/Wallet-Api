package com.walletapi.demo.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walletapi.demo.application.dto.GoalBoxCreateDTO;
import com.walletapi.demo.application.dto.GoalBoxDepositDTO;
import com.walletapi.demo.application.dto.GoalBoxUpdateDTO;
import com.walletapi.demo.application.dto.GoalBoxWithdrawDTO;
import com.walletapi.demo.application.exceptions.GoalBoxNotFoundException;
import com.walletapi.demo.application.exceptions.InsufficientBalanceException;
import com.walletapi.demo.application.service.GoalBoxService;
import com.walletapi.demo.application.service.TokenService;
import com.walletapi.demo.domain.entities.GoalBox;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GoalBoxController.class)
@Import(SecurityConfigurations.class)
class GoalBoxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoalBoxService boxService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserCredentialsRepository userCredentialsRepository;

    private UserCredentials credentials;
    private User user;

    private static final String FAKE_TOKEN = "Bearer fake-jwt-token";

    @BeforeEach
    void setUp() {
        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal("1000.00"));
        wallet.setStatus(WalletStatus.ACTIVE);

        user = new User();
        user.setId(1L);
        user.setName("Gabriel");
        user.setEmail("gabriel@email.com");
        user.setWallet(wallet);

        credentials = new UserCredentials();
        credentials.setId("uuid-teste");
        credentials.setLogin("gabriel@email.com");
        credentials.setPassword("senha_hash");
        credentials.setRole(UserRole.USER);
        credentials.setUser(user);

        when(tokenService.validateToken("fake-jwt-token")).thenReturn("gabriel@email.com");

        when(userCredentialsRepository.findByLogin("gabriel@email.com")).thenReturn(credentials);
    }

    @Test
    @DisplayName("createBox: deve retornar 201 e o DTO da caixinha criada quando dados são válidos")
    void createBox_dadosValidos_retorna201() throws Exception {
        GoalBoxCreateDTO dto = new GoalBoxCreateDTO("Viagem", "Férias em Floripa", new BigDecimal("2000.00"));

        GoalBox box = new GoalBox();
        box.setId(1L);
        box.setName("Viagem");
        box.setDescription("Férias em Floripa");
        box.setTargetAmount(new BigDecimal("2000.00"));
        box.setCurrentBalance(BigDecimal.ZERO);
        box.setUser(user);

        when(boxService.createBox(any(User.class), any(GoalBoxCreateDTO.class))).thenReturn(box);

        mockMvc.perform(post("/api/goal-boxes")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Viagem"))
                .andExpect(jsonPath("$.progress").value("0%"));

        verify(boxService).createBox(any(User.class), any(GoalBoxCreateDTO.class));
    }

    @Test
    @DisplayName("createBox: deve retornar 400 quando name está em branco")
    void createBox_nameBranco_retorna400() throws Exception {
        GoalBoxCreateDTO dto = new GoalBoxCreateDTO("", "desc", new BigDecimal("500.00"));

        mockMvc.perform(post("/api/goal-boxes")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(boxService);
    }

    @Test
    @DisplayName("createBox: deve retornar 400 quando targetAmount é nulo")
    void createBox_targetAmountNulo_retorna400() throws Exception {
        GoalBoxCreateDTO dto = new GoalBoxCreateDTO("Viagem", "desc", null);

        mockMvc.perform(post("/api/goal-boxes")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(boxService);
    }

    @Test
    @DisplayName("createBox: deve retornar 400 quando targetAmount é negativo")
    void createBox_targetAmountNaoPositivo_retorna400() throws Exception {
        GoalBoxCreateDTO dto = new GoalBoxCreateDTO("Viagem", "desc", new BigDecimal("-100.00"));

        mockMvc.perform(post("/api/goal-boxes")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(boxService);
    }

    @Test
    @DisplayName("getBox: deve retornar 200 e o DTO quando a caixinha pertence ao usuário")
    void getBox_caixinhaExiste_retorna200() throws Exception {
        GoalBox box = new GoalBox();
        box.setId(10L);
        box.setName("Emergência");
        box.setDescription("Reserva");
        box.setTargetAmount(new BigDecimal("5000.00"));
        box.setCurrentBalance(new BigDecimal("1000.00"));
        box.setUser(user);

        when(boxService.getBox(any(User.class), eq(10L))).thenReturn(box);

        mockMvc.perform(get("/api/goal-boxes/10")
                        .header("Authorization", FAKE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Emergência"))
                .andExpect(jsonPath("$.progress").value("20%"));

        verify(boxService).getBox(any(User.class), eq(10L));
    }

    @Test
    @DisplayName("getBox: deve retornar 404 quando a caixinha não existe ou não pertence ao usuário")
    void getBox_caixinhaNaoEncontrada_retorna404() throws Exception {
        when(boxService.getBox(any(User.class), eq(99L)))
                .thenThrow(new GoalBoxNotFoundException(99L));

        mockMvc.perform(get("/api/goal-boxes/99")
                        .header("Authorization", FAKE_TOKEN))
                .andExpect(status().isNotFound());

        verify(boxService).getBox(any(User.class), eq(99L));
    }

    @Test
    @DisplayName("deposit: deve retornar 200 e o DTO atualizado quando depósito é válido")
    void deposit_valorValido_retorna200() throws Exception {
        GoalBoxDepositDTO dto = new GoalBoxDepositDTO(new BigDecimal("200.00"));

        GoalBox box = new GoalBox();
        box.setId(10L);
        box.setName("Viagem");
        box.setTargetAmount(new BigDecimal("1000.00"));
        box.setCurrentBalance(new BigDecimal("200.00"));
        box.setUser(user);

        when(boxService.deposit(any(User.class), eq(10L), any(BigDecimal.class))).thenReturn(box);

        mockMvc.perform(post("/api/goal-boxes/10/deposit")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(200.00))
                .andExpect(jsonPath("$.progress").value("20%"));

        verify(boxService).deposit(any(User.class), eq(10L), any(BigDecimal.class));
    }

    @Test
    @DisplayName("deposit: deve retornar 400 quando amount é nulo")
    void deposit_amountNulo_retorna400() throws Exception {
        GoalBoxDepositDTO dto = new GoalBoxDepositDTO(null);

        mockMvc.perform(post("/api/goal-boxes/10/deposit")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(boxService);
    }

    @Test
    @DisplayName("deposit: deve retornar 400 quando amount é negativo")
    void deposit_amountNegativo_retorna400() throws Exception {
        GoalBoxDepositDTO dto = new GoalBoxDepositDTO(new BigDecimal("-50.00"));

        mockMvc.perform(post("/api/goal-boxes/10/deposit")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(boxService);
    }

    @Test
    @DisplayName("deposit: deve retornar 422 quando saldo da carteira é insuficiente")
    void deposit_saldoInsuficiente_retorna422() throws Exception {
        GoalBoxDepositDTO dto = new GoalBoxDepositDTO(new BigDecimal("9999.00"));

        when(boxService.deposit(any(User.class), eq(10L), any(BigDecimal.class)))
                .thenThrow(new InsufficientBalanceException());

        mockMvc.perform(post("/api/goal-boxes/10/deposit")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());

        verify(boxService).deposit(any(User.class), eq(10L), any(BigDecimal.class));
    }

    @Test
    @DisplayName("deposit: deve retornar 404 quando a caixinha não existe")
    void deposit_caixinhaNaoEncontrada_retorna404() throws Exception {
        GoalBoxDepositDTO dto = new GoalBoxDepositDTO(new BigDecimal("100.00"));

        when(boxService.deposit(any(User.class), eq(99L), any(BigDecimal.class)))
                .thenThrow(new GoalBoxNotFoundException(99L));

        mockMvc.perform(post("/api/goal-boxes/99/deposit")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(boxService).deposit(any(User.class), eq(99L), any(BigDecimal.class));
    }

    @Test
    @DisplayName("withdraw: deve retornar 200 e o DTO atualizado quando saque é válido")
    void withdraw_valorValido_retorna200() throws Exception {
        GoalBoxWithdrawDTO dto = new GoalBoxWithdrawDTO(new BigDecimal("100.00"));

        GoalBox box = new GoalBox();
        box.setId(10L);
        box.setName("Viagem");
        box.setTargetAmount(new BigDecimal("1000.00"));
        box.setCurrentBalance(new BigDecimal("400.00"));
        box.setUser(user);

        when(boxService.withdraw(any(User.class), eq(10L), any(BigDecimal.class))).thenReturn(box);

        mockMvc.perform(post("/api/goal-boxes/10/withdraw")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(400.00));

        verify(boxService).withdraw(any(User.class), eq(10L), any(BigDecimal.class));
    }

    @Test
    @DisplayName("withdraw: deve retornar 400 quando amount é nulo")
    void withdraw_amountNulo_retorna400() throws Exception {
        GoalBoxWithdrawDTO dto = new GoalBoxWithdrawDTO(null);

        mockMvc.perform(post("/api/goal-boxes/10/withdraw")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(boxService);
    }

    @Test
    @DisplayName("withdraw: deve retornar 422 quando saldo da caixinha é insuficiente")
    void withdraw_saldoInsuficiente_retorna422() throws Exception {
        GoalBoxWithdrawDTO dto = new GoalBoxWithdrawDTO(new BigDecimal("9999.00"));

        when(boxService.withdraw(any(User.class), eq(10L), any(BigDecimal.class)))
                .thenThrow(new InsufficientBalanceException());

        mockMvc.perform(post("/api/goal-boxes/10/withdraw")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());

        verify(boxService).withdraw(any(User.class), eq(10L), any(BigDecimal.class));
    }

    @Test
    @DisplayName("updateBox: deve retornar 200 e o DTO atualizado quando dados são válidos")
    void updateBox_dadosValidos_retorna200() throws Exception {
        GoalBoxUpdateDTO dto = new GoalBoxUpdateDTO("Novo Nome", null, null);

        GoalBox box = new GoalBox();
        box.setId(10L);
        box.setName("Novo Nome");
        box.setTargetAmount(new BigDecimal("1000.00"));
        box.setCurrentBalance(BigDecimal.ZERO);
        box.setUser(user);

        when(boxService.updateBox(any(User.class), eq(10L), any(GoalBoxUpdateDTO.class))).thenReturn(box);

        mockMvc.perform(patch("/api/goal-boxes/10")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Novo Nome"));

        verify(boxService).updateBox(any(User.class), eq(10L), any(GoalBoxUpdateDTO.class));
    }

    @Test
    @DisplayName("updateBox: deve retornar 400 quando targetAmount é negativo")
    void updateBox_targetAmountNegativo_retorna400() throws Exception {
        GoalBoxUpdateDTO dto = new GoalBoxUpdateDTO(null, null, new BigDecimal("-100.00"));

        mockMvc.perform(patch("/api/goal-boxes/10")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(boxService);
    }

    @Test
    @DisplayName("updateBox: deve retornar 404 quando a caixinha não existe")
    void updateBox_caixinhaNaoEncontrada_retorna404() throws Exception {
        GoalBoxUpdateDTO dto = new GoalBoxUpdateDTO("Nome", null, null);

        when(boxService.updateBox(any(User.class), eq(99L), any(GoalBoxUpdateDTO.class)))
                .thenThrow(new GoalBoxNotFoundException(99L));

        mockMvc.perform(patch("/api/goal-boxes/99")
                        .header("Authorization", FAKE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(boxService).updateBox(any(User.class), eq(99L), any(GoalBoxUpdateDTO.class));
    }

    @Test
    @DisplayName("deleteBox: deve retornar 204 quando a caixinha existe e pertence ao usuário")
    void deleteBox_caixinhaExiste_retorna204() throws Exception {
        doNothing().when(boxService).deleteBox(any(User.class), eq(10L));

        mockMvc.perform(delete("/api/goal-boxes/10")
                        .header("Authorization", FAKE_TOKEN))
                .andExpect(status().isNoContent());

        verify(boxService).deleteBox(any(User.class), eq(10L));
    }

    @Test
    @DisplayName("deleteBox: deve retornar 404 quando a caixinha não existe")
    void deleteBox_caixinhaNaoEncontrada_retorna404() throws Exception {
        doThrow(new GoalBoxNotFoundException(99L)).when(boxService).deleteBox(any(User.class), eq(99L));

        mockMvc.perform(delete("/api/goal-boxes/99")
                        .header("Authorization", FAKE_TOKEN))
                .andExpect(status().isNotFound());

        verify(boxService).deleteBox(any(User.class), eq(99L));
    }
}