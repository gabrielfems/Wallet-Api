package com.walletapi.demo.application.controller;

import com.walletapi.demo.application.dto.*;
import com.walletapi.demo.application.service.GoalBoxService;
import com.walletapi.demo.domain.entities.GoalBox;
import com.walletapi.demo.domain.entities.UserCredentials;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/goal-boxes")
@Tag(name = "Caixinha", description = "Caixinha de metas do usuário")
public class GoalBoxController {

    private final GoalBoxService boxService;

    @PostMapping
    @Operation(summary = "Criar caixinha", description = "Cria uma caixinha de meta para o usuário autenticado")
    @ApiResponse(responseCode = "201", description = "Caixinha criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou dados obrigatórios ausentes")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    public ResponseEntity<GoalBoxResponseDTO> createBox(
            @AuthenticationPrincipal UserCredentials credentials,
            @Valid @RequestBody GoalBoxCreateDTO dto) {

        GoalBox box = boxService.createBox(credentials.getUser(), dto);
        return new ResponseEntity<>(GoalBoxResponseDTO.from(box), HttpStatus.CREATED);
    }

    @GetMapping("/list")
    @Operation(summary = "Listar caixinhas", description = "Exibe todas as caixinhas do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Caixinhas encontradas com sucesso")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    public ResponseEntity<List<GoalBoxResponseDTO>> getUserBoxes(
            @AuthenticationPrincipal UserCredentials credentials) {

        return ResponseEntity.ok(boxService.getUserBoxes(credentials.getUser()));
    }

    @GetMapping("/{boxId}")
    @Operation(summary = "Exibir caixinha", description = "Retorna uma caixinha específica do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Caixinha encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Caixinha não encontrada ou não pertence ao usuário")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    public ResponseEntity<GoalBoxResponseDTO> getBox(
            @AuthenticationPrincipal UserCredentials credentials,
            @PathVariable Long boxId) {

        GoalBox box = boxService.getBox(credentials.getUser(), boxId);
        return ResponseEntity.ok(GoalBoxResponseDTO.from(box));
    }

    @PostMapping("/{boxId}/deposit")
    @Operation(summary = "Depositar na caixinha", description = "Adiciona dinheiro na caixinha e atualiza o status da meta")
    @ApiResponse(responseCode = "200", description = "Depósito realizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou dados obrigatórios ausentes")
    @ApiResponse(responseCode = "404", description = "Caixinha não encontrada ou não pertence ao usuário")
    @ApiResponse(responseCode = "422", description = "Saldo insuficiente na carteira")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    public ResponseEntity<GoalBoxResponseDTO> deposit(
            @AuthenticationPrincipal UserCredentials credentials,
            @PathVariable Long boxId,
            @Valid @RequestBody GoalBoxDepositDTO dto) {

        GoalBox box = boxService.deposit(credentials.getUser(), boxId, dto.amount());
        return ResponseEntity.ok(GoalBoxResponseDTO.from(box));
    }

    @PostMapping("/{boxId}/withdraw")
    @Operation(summary = "Sacar da caixinha", description = "Retira dinheiro da caixinha e atualiza o status da meta")
    @ApiResponse(responseCode = "200", description = "Saque realizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou dados obrigatórios ausentes")
    @ApiResponse(responseCode = "404", description = "Caixinha não encontrada ou não pertence ao usuário")
    @ApiResponse(responseCode = "422", description = "Saldo insuficiente na caixinha")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    public ResponseEntity<GoalBoxResponseDTO> withdraw(
            @AuthenticationPrincipal UserCredentials credentials,
            @PathVariable Long boxId,
            @Valid @RequestBody GoalBoxWithdrawDTO dto) {

        GoalBox box = boxService.withdraw(credentials.getUser(), boxId, dto.amount());
        return ResponseEntity.ok(GoalBoxResponseDTO.from(box));
    }

    @PatchMapping("/{boxId}")
    @Operation(summary = "Atualizar caixinha", description = "Atualiza os dados de uma caixinha do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Caixinha atualizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou dados obrigatórios ausentes")
    @ApiResponse(responseCode = "404", description = "Caixinha não encontrada ou não pertence ao usuário")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    public ResponseEntity<GoalBoxResponseDTO> updateBox(
            @AuthenticationPrincipal UserCredentials credentials,
            @PathVariable Long boxId,
            @Valid @RequestBody GoalBoxUpdateDTO dto) {

        GoalBox box = boxService.updateBox(credentials.getUser(), boxId, dto);
        return ResponseEntity.ok(GoalBoxResponseDTO.from(box));
    }

    @DeleteMapping("/{boxId}")
    @Operation(summary = "Excluir caixinha", description = "Exclui uma caixinha do usuário autenticado")
    @ApiResponse(responseCode = "204", description = "Caixinha excluída com sucesso")
    @ApiResponse(responseCode = "404", description = "Caixinha não encontrada ou não pertence ao usuário")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    public ResponseEntity<Void> deleteBox(
            @AuthenticationPrincipal UserCredentials credentials,
            @PathVariable Long boxId) {

        boxService.deleteBox(credentials.getUser(), boxId);
        return ResponseEntity.noContent().build();
    }
}
