package com.walletapi.demo.application.controller;

import com.walletapi.demo.application.dto.*;
import com.walletapi.demo.domain.entities.GoalBox;
import com.walletapi.demo.application.service.GoalBoxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/goal-boxes")
@Tag(name= "Caixinha", description= "Caixinha de metas do usuário")
public class GoalBoxController {

    @Autowired
    private GoalBoxService boxService;

    @PostMapping
    @Operation(summary= "Criar caixinha", description= "Cria caixinha com metas pessoais do usuário")
    @ApiResponse(responseCode = "201", description = "Caixinha criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida, mal formatada ou faltando dados obrigatórios")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<GoalBoxResponseDTO> createBox(@PathVariable Long userId, @Valid @RequestBody GoalBoxCreateDTO dto) {
        GoalBox box = boxService.createBox(userId, dto);
        return new ResponseEntity<>(GoalBoxResponseDTO.from(box), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary= "Listar caixinhas", description= "Exibe todas as caixinhas cadastradas de todos os usuários da base de dados")
    @ApiResponse(responseCode = "201", description = "Caixinhas encontradas com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida, mal formatada ou faltando dados obrigatórios")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<List<GoalBoxResponseDTO>> getUserBoxes(@PathVariable Long userId) {
        return ResponseEntity.ok(boxService.getUserBoxes(userId));
    }

    @GetMapping("/{boxId}")
    @Operation(summary= "Exibir caixinha", description= "Filtra uma caixinha específica pelo Id do usuário e Id da caixinha")
    @ApiResponse(responseCode = "201", description = "Caixinha encontrada com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida, mal formatada ou faltando dados obrigatórios")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<GoalBoxResponseDTO> getBox(@PathVariable Long userId, @PathVariable Long boxId) {
        GoalBox box = boxService.getBox(userId, boxId);
        return ResponseEntity.ok(GoalBoxResponseDTO.from(box));
    }

    @PostMapping("/{boxId}/deposit")
    @Operation(summary= "Adicionar dinheiro", description= "Adiciona dinheiro na caixinha e atualiza o status da meta")
    @ApiResponse(responseCode = "201", description = "Dinheiro depositado com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida, mal formatada ou faltando dados obrigatórios")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<GoalBoxResponseDTO> deposit(@PathVariable Long userId, @PathVariable Long boxId, @Valid @RequestBody GoalBoxDepositDTO dto) {
        GoalBox box = boxService.deposit(userId, boxId, dto.amount());
        return ResponseEntity.ok(GoalBoxResponseDTO.from(box));
    }

        @DeleteMapping("/{boxId}")
        @Operation(summary= "Excluir caixinha", description= "Exclui caixinha")
        @ApiResponse(responseCode = "201", description = "Caixinha excluída com sucesso")
        @ApiResponse(responseCode = "400", description = "Requisição inválida, mal formatada ou faltando dados obrigatórios")
        @ApiResponse(responseCode = "500", description = "Erro no servidor")
        public ResponseEntity<Void> deleteBox(@PathVariable Long userId, @PathVariable Long boxId) {
        boxService.deleteBox(userId, boxId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{boxId}/withdraw")
    @Operation(summary= "Sacar dinheiro", description= "Tira dinheiro da caixinha e atualiza o status da meta")
    @ApiResponse(responseCode = "201", description = "Dinheiro sacado com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida, mal formatada ou faltando dados obrigatórios")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<GoalBoxResponseDTO> withdraw(@PathVariable Long userId, @PathVariable Long boxId, @Valid @RequestBody GoalBoxWithdrawDTO dto) {
        GoalBox box = boxService.withdraw(userId, boxId, dto.amount());
        return ResponseEntity.ok(GoalBoxResponseDTO.from(box));
    }

    @PatchMapping("/{boxId}")
    @Operation(summary= "Atualizar caixinha", description= "Atualiza dados da caixinha")
    @ApiResponse(responseCode = "201", description = "Caixinha atualizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida, mal formatada ou faltando dados obrigatórios")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<GoalBoxResponseDTO> updateBox(@PathVariable Long userId, @PathVariable Long boxId, @Valid @RequestBody GoalBoxUpdateDTO dto) {
        GoalBox box = boxService.updateBox(userId, boxId, dto);
        return ResponseEntity.ok(GoalBoxResponseDTO.from(box));
    }
}