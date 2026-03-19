package com.walletapi.demo.application.controller;

import com.walletapi.demo.application.dto.TransactionDepositDTO;
import com.walletapi.demo.application.dto.TransactionTransferDTO;
import com.walletapi.demo.application.dto.TransactionResponseDTO;
import com.walletapi.demo.application.dto.TransactionWithdrawDTO;
import com.walletapi.demo.application.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDTO> createTransfer(@Valid @RequestBody TransactionTransferDTO data) {
        TransactionResponseDTO response = TransactionResponseDTO.from(
                transactionService.createTransfer(data)
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponseDTO> createDeposit(@Valid @RequestBody TransactionDepositDTO data) {
        TransactionResponseDTO response = TransactionResponseDTO.from(
                transactionService.createDeposit(data)
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponseDTO> createWithdraw(@Valid @RequestBody TransactionWithdrawDTO data) {
        TransactionResponseDTO response = TransactionResponseDTO.from(
                transactionService.createWithdraw(data)
        );

        return ResponseEntity.ok(response);
    }
}
