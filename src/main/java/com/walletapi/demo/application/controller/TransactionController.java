package com.walletapi.demo.application.controller;

import com.walletapi.demo.application.dto.TransactionDepositDTO;
import com.walletapi.demo.application.dto.TransactionResponseDTO;
import com.walletapi.demo.application.dto.TransactionTransferDTO;
import com.walletapi.demo.application.dto.TransactionWithdrawDTO;
import com.walletapi.demo.application.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/transactions")
@Tag(name= "Transactions", description= "Realiza uma transação, o usuário pode escolher entre Depósito, " +
        "Transferência ou Saque")
public class TransactionController {

    @Autowired
    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @Operation(summary= "Transferência", description= "Realiza uma transação entre usuários cadastrados")
    @ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou tipo de transação inválido")
    @ApiResponse(responseCode = "404", description = "Usuário remetente ou destinatário não encontrado")
    @ApiResponse(responseCode = "422", description = "Saldo insuficiente para transferência")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    public ResponseEntity<TransactionResponseDTO> createTransfer(@Valid @RequestBody TransactionTransferDTO data) {
        TransactionResponseDTO response = TransactionResponseDTO.from(
                transactionService.createTransfer(data)
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposit")
    @Operation(summary= "Depósito", description= "Deposita dinheiro em uma conta específica")
    @ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou tipo de transação inválido")
    @ApiResponse(responseCode = "404", description = "Usuário remetente ou destinatário não encontrado")
    @ApiResponse(responseCode = "422", description = "Saldo insuficiente para transferência")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    public ResponseEntity<TransactionResponseDTO> createDeposit(@Valid @RequestBody TransactionDepositDTO data) {
        TransactionResponseDTO response = TransactionResponseDTO.from(
                transactionService.createDeposit(data)
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    @Operation(summary= "Saque", description= "Saca o dinheiro presente na conta")
    @ApiResponse(responseCode = "200", description = "Saque realizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou tipo de transação inválido")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "422", description = "Saldo insuficiente para saque")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    public ResponseEntity<TransactionResponseDTO> createWithdraw(@Valid @RequestBody TransactionWithdrawDTO data) {
        TransactionResponseDTO response = TransactionResponseDTO.from(
                transactionService.createWithdraw(data)
        );

        return ResponseEntity.ok(response);
    }
}
