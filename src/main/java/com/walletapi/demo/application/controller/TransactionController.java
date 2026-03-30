package com.walletapi.demo.application.controller;

import com.walletapi.demo.application.dto.TransactionDepositDTO;
import com.walletapi.demo.application.dto.TransactionTransferDTO;
import com.walletapi.demo.application.dto.TransactionResponseDTO;
import com.walletapi.demo.application.dto.TransactionWithdrawDTO;
import com.walletapi.demo.application.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@Tag(name= "Transactions", description= "Realiza uma transação, o usuário pode escolher entre Depósito, " +
        "Transferência ou Saque")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transfer")
    @Operation(summary= "Transferência", description= "Realiza uma transação entre usuários cadastrados")
    public ResponseEntity<TransactionResponseDTO> createTransfer(@Valid @RequestBody TransactionTransferDTO data) {
        TransactionResponseDTO response = TransactionResponseDTO.from(
                transactionService.createTransfer(data)
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposit")
    @Operation(summary= "Depósito", description= "Deposita dinheiro em uma conta específica")
    public ResponseEntity<TransactionResponseDTO> createDeposit(@Valid @RequestBody TransactionDepositDTO data) {
        TransactionResponseDTO response = TransactionResponseDTO.from(
                transactionService.createDeposit(data)
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    @Operation(summary= "Saque", description= "Saca o dinheiro presente na conta")
    public ResponseEntity<TransactionResponseDTO> createWithdraw(@Valid @RequestBody TransactionWithdrawDTO data) {
        TransactionResponseDTO response = TransactionResponseDTO.from(
                transactionService.createWithdraw(data)
        );

        return ResponseEntity.ok(response);
    }
}
