package com.walletapi.demo.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walletapi.demo.application.dto.GoalBoxCreateDTO;
import com.walletapi.demo.application.dto.GoalBoxDepositDTO;
import com.walletapi.demo.application.dto.GoalBoxResponseDTO;
import com.walletapi.demo.application.dto.GoalBoxWithdrawDTO;
import com.walletapi.demo.application.exceptions.GoalBoxNotFoundException;
import com.walletapi.demo.application.exceptions.InsufficientBalanceException;
import com.walletapi.demo.application.exceptions.UnauthorizedBoxAccessException;
import com.walletapi.demo.application.exceptions.UserNotFoundException;
import com.walletapi.demo.application.service.GoalBoxService;
import com.walletapi.demo.domain.entities.GoalBox;
import com.walletapi.demo.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GoalBoxController.class)
class GoalBoxControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    GoalBoxService boxService;

    @Autowired
    ObjectMapper objectMapper;

    private User user;
    private GoalBox goalBox;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        goalBox = new GoalBox();
        goalBox.setId(1L);
        goalBox.setName("Viagem");
        goalBox.setDescription("Viagem para o Japão");
        goalBox.setTargetAmount(BigDecimal.valueOf(10000));
        goalBox.setCurrentBalance(BigDecimal.ZERO);
        goalBox.setUser(user);
    }

    @Test
    @DisplayName("Should return 201 when box is created successfully")
    void createBoxCase1() throws Exception {
        GoalBoxCreateDTO dto = new GoalBoxCreateDTO("Viagem", "Viagem para o Japão", BigDecimal.valueOf(10000));

        when(boxService.createBox(1L, dto)).thenReturn(goalBox);

        mockMvc.perform(post("/api/users/1/goal-boxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Viagem"))
                .andExpect(jsonPath("$.targetAmount").value(10000))
                .andExpect(jsonPath("$.progress").value("0%"));
    }

    @Test
    @DisplayName("Should return 400 when name is blank")
    void createBoxCase2() throws Exception {
        GoalBoxCreateDTO dto = new GoalBoxCreateDTO("", "Descrição", BigDecimal.valueOf(1000));

        mockMvc.perform(post("/api/users/1/goal-boxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when targetAmount is null")
    void createBoxCase3() throws Exception {
        GoalBoxCreateDTO dto = new GoalBoxCreateDTO("Viagem", "Descrição", null);

        mockMvc.perform(post("/api/users/1/goal-boxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when user is not found")
    void createBoxCase4() throws Exception {
        GoalBoxCreateDTO dto = new GoalBoxCreateDTO("Viagem", "Descrição", BigDecimal.valueOf(1000));

        when(boxService.createBox(1L, dto)).thenThrow(new UserNotFoundException(1L));

        mockMvc.perform(post("/api/users/1/goal-boxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 200 when boxes have founds")
    void getUserBoxesCase1() throws Exception {
        GoalBoxResponseDTO responseDTO = new GoalBoxResponseDTO(
                1L, "Viagem", "Descrição", BigDecimal.valueOf(1000),
                BigDecimal.ZERO, "0%", 1L);

        when((boxService.getUserBoxes(1L))).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/users/1/goal-boxes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Viagem"))
                .andExpect(jsonPath("$[0].progress").value("0%"));
    }

    @Test
    @DisplayName("Should return 400")
    void getUserBoxesCase2() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/goal-boxes", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 200 when goal box ID and the user ID are valid")
    void getBoxCase1() throws Exception {

        when(boxService.getBox(1L, 1L)).thenReturn(goalBox);

        mockMvc.perform(get("/api/users/{userId}/goal-boxes/{boxId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Viagem"))
                .andExpect(jsonPath("$.progress").value("0%"));
    }

    @Test
    @DisplayName("Should return 403 when box does not belong to the given user")
    void getBoxCase2() throws Exception {

        doThrow(new UnauthorizedBoxAccessException()).when(boxService).getBox(1L, 99L);

        mockMvc.perform(get("/api/users/{userId}/goal-boxes/{boxId}", 1L, 99L))
                .andExpect(status().isForbidden());

        verify(boxService).getBox(1L, 99L);
    }

    @Test
    @DisplayName("Should return 404 when box not found")
    void getBoxCase3() throws Exception {

        doThrow(new GoalBoxNotFoundException(1L)).when(boxService).getBox(1L, 99L);

        mockMvc.perform(get("/api/users/{userId}/goal-boxes/{boxId}", 1L, 99L))
                .andExpect(status().isNotFound());

        verify(boxService).getBox(1L, 99L);
    }

    @Test
    @DisplayName("Should return 200 when deposit have succefully")
    void depositCase1() throws Exception{
        goalBox.setCurrentBalance(BigDecimal.valueOf(10000));

        GoalBoxDepositDTO dto = new GoalBoxDepositDTO(BigDecimal.valueOf(10000));

        when(boxService.deposit(1L, 1L, BigDecimal.valueOf(10000))).thenReturn(goalBox);

        mockMvc.perform(post("/api/users/{userId}/goal-boxes/{boxId}/deposit", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(10000));

        verify(boxService).deposit(1L, 1L, BigDecimal.valueOf(10000));
    }

    @Test
    @DisplayName("Should return 400 when amount is null")
    void depositCase2() throws Exception {
        GoalBoxDepositDTO dto = new GoalBoxDepositDTO(null);

        mockMvc.perform(post("/api/users/{userId}/goal-boxes/{boxId}/deposit", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(boxService);
    }

    @Test
    @DisplayName("Should return 400 when amount is zero")
    void depositCase3() throws Exception {
        GoalBoxDepositDTO dto = new GoalBoxDepositDTO(BigDecimal.ZERO);

        mockMvc.perform(post("/api/users/{userId}/goal-boxes/{boxId}/deposit", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(boxService);
    }

    @Test
    @DisplayName("Should return 403 when box does not belong to the given user")
    void depositCase4() throws Exception {

        GoalBoxDepositDTO dto = new GoalBoxDepositDTO(BigDecimal.valueOf(1000));

        doThrow(new UnauthorizedBoxAccessException()).when(boxService).deposit(1L,
                99L, BigDecimal.valueOf(1000));

        mockMvc.perform(post("/api/users/{userId}/goal-boxes/{boxId}/deposit", 1L, 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(boxService).deposit(1L, 99L, BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void depositCase5() throws Exception {

        GoalBoxDepositDTO dto = new GoalBoxDepositDTO(BigDecimal.valueOf(1000));

        doThrow(new UserNotFoundException(99L)).when(boxService).deposit(99L,
                1L, BigDecimal.valueOf(1000));

        mockMvc.perform(post("/api/users/{userId}/goal-boxes/{boxId}/deposit", 99L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(boxService).deposit(99L, 1L, BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("Should return 404 when box not found")
    void depositCase6() throws Exception {
        GoalBoxDepositDTO dto = new GoalBoxDepositDTO(BigDecimal.valueOf(1000));

        doThrow(new GoalBoxNotFoundException(99L)).when(boxService).deposit(1L,
                99L, BigDecimal.valueOf(1000));

        mockMvc.perform(post("/api/users/{userId}/goal-boxes/{boxId}/deposit", 1L, 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(boxService).deposit(1L, 99L, BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("Should return 422 when insufficient funds")
    void depositCase7() throws Exception {

        GoalBoxDepositDTO dto = new GoalBoxDepositDTO(BigDecimal.valueOf(99999));

        doThrow(new InsufficientBalanceException()).when(boxService).deposit(1L,
                1L, BigDecimal.valueOf(99999));

        mockMvc.perform(post("/api/users/{userId}/goal-boxes/{boxId}/deposit", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());

        verify(boxService).deposit(1L, 1L, BigDecimal.valueOf(99999));
    }

    @Test
    @DisplayName("Should return 204 when box deleted succefully")
    void deleteBoxCase1() throws Exception {
        doNothing().when(boxService).deleteBox(1L, 1L);

        mockMvc.perform(delete("/api/users/{userId}/goal-boxes/{boxId}", 1L, 1L))
                .andExpect(status().isNoContent());

        verify(boxService).deleteBox(1L, 1L);
    }

    @Test
    @DisplayName("Should return 403 when box does not belong to the given user")
    void deleteBoxCase2() throws Exception {
        doThrow(new UnauthorizedBoxAccessException()).when(boxService).deleteBox(1L, 99L);

        mockMvc.perform(delete("/api/users/{userId}/goal-boxes/{boxId}", 1L, 99L))
                .andExpect(status().isForbidden());

        verify(boxService).deleteBox(1L, 99L);
    }

    @Test
    @DisplayName("Should return 404 when box not found")
    void deleteBoxCase3() throws Exception {
        doThrow(new GoalBoxNotFoundException(99L)).when(boxService).deleteBox(1L, 99L);

        mockMvc.perform(delete("/api/users/{userId}/goal-boxes/{boxId}", 1L, 99L))
                .andExpect(status().isNotFound());

        verify(boxService).deleteBox(1L, 99L);
    }

    @Test
    @DisplayName("Should return 200 when withdraw have been succefully")
    void withdrawCase1() throws Exception{
        GoalBoxWithdrawDTO dto = new GoalBoxWithdrawDTO(BigDecimal.valueOf(10000));

        when(boxService.withdraw(1L, 1L, BigDecimal.valueOf(10000))).thenReturn(goalBox);

        mockMvc.perform(post("/api/users/{userId}/goal-boxes/{boxId}/withdraw", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(0));

        verify(boxService).withdraw(1L, 1L, BigDecimal.valueOf(10000));

    }

    @Test
    @DisplayName("Should return 400 when amount is null")
    void withdrawCase2() throws Exception {
        GoalBoxWithdrawDTO dto = new GoalBoxWithdrawDTO(null);

        mockMvc.perform(post("/api/users/{userId}/goal-boxes/{boxId}/withdraw", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(boxService);
    }

    @Test
    @DisplayName("Should return 400 when amount is zero")
    void withdrawCase3() throws Exception {
        GoalBoxWithdrawDTO dto = new GoalBoxWithdrawDTO(BigDecimal.ZERO);

        mockMvc.perform(post("/api/users/{userId}/goal-boxes/{boxId}/withdraw", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(boxService);
    }

    @Test
    @DisplayName("Should return 403 when box does not belong to the given user")
    void withdrawCase4() throws Exception {

        GoalBoxWithdrawDTO dto = new GoalBoxWithdrawDTO(BigDecimal.valueOf(1000));

        doThrow(new UnauthorizedBoxAccessException()).when(boxService).withdraw(1L,
                99L, BigDecimal.valueOf(1000));

        mockMvc.perform(post("/api/users/{userId}/goal-boxes/{boxId}/withdraw", 1L, 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(boxService).withdraw(1L, 99L, BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void withdrawCase5() throws Exception {

        GoalBoxWithdrawDTO dto = new GoalBoxWithdrawDTO(BigDecimal.valueOf(1000));

        doThrow(new UserNotFoundException(99L)).when(boxService).withdraw(99L,
                1L, BigDecimal.valueOf(1000));

        mockMvc.perform(post("/api/users/{userId}/goal-boxes/{boxId}/withdraw", 99L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(boxService).withdraw(99L, 1L, BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("Should return 404 when box not found")
    void withdrawCase6() throws Exception {
        GoalBoxWithdrawDTO dto = new GoalBoxWithdrawDTO(BigDecimal.valueOf(1000));

        doThrow(new GoalBoxNotFoundException(99L)).when(boxService).withdraw(1L,
                99L, BigDecimal.valueOf(1000));

        mockMvc.perform(post("/api/users/{userId}/goal-boxes/{boxId}/withdraw", 1L, 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(boxService).withdraw(1L, 99L, BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("Should return 422 when insufficient funds")
    void withdrawCase7() throws Exception {

        GoalBoxWithdrawDTO dto = new GoalBoxWithdrawDTO(BigDecimal.valueOf(99999));

        doThrow(new InsufficientBalanceException()).when(boxService).withdraw(1L,
                1L, BigDecimal.valueOf(99999));

        mockMvc.perform(post("/api/users/{userId}/goal-boxes/{boxId}/withdraw", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());

        verify(boxService).withdraw(1L, 1L, BigDecimal.valueOf(99999));
    }

    @Test
    @DisplayName("Should return 200 when box updated succefully")
    void updateBoxCase1() {
    }
}