package com.walletapi.demo.application.controller;

import com.walletapi.demo.application.dto.*;
import com.walletapi.demo.domain.entities.GoalBox;
import com.walletapi.demo.application.service.GoalBoxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/goal-boxes")
public class GoalBoxController {

    @Autowired
    private GoalBoxService boxService;

    @PostMapping
    public ResponseEntity<GoalBoxResponseDTO> createBox(@PathVariable Long userId, @Valid @RequestBody GoalBoxCreateDTO dto) {
        GoalBox box = boxService.createBox(userId, dto);
        return new ResponseEntity<>(GoalBoxResponseDTO.from(box), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<GoalBoxResponseDTO>> getUserBoxes(@PathVariable Long userId) {
        return ResponseEntity.ok(boxService.getUserBoxes(userId));
    }

    @GetMapping("/{boxId}")
    public ResponseEntity<GoalBoxResponseDTO> getBox(@PathVariable Long userId, @PathVariable Long boxId) {
        GoalBox box = boxService.getBox(userId, boxId);
        return ResponseEntity.ok(GoalBoxResponseDTO.from(box));
    }

    @PostMapping("/{boxId}/deposit")
    public ResponseEntity<GoalBoxResponseDTO> deposit(@PathVariable Long userId, @PathVariable Long boxId, @Valid @RequestBody GoalBoxDepositDTO dto) {
        GoalBox box = boxService.deposit(userId, boxId, dto.amount());
        return ResponseEntity.ok(GoalBoxResponseDTO.from(box));
    }

        @DeleteMapping("/{boxId}")
    public ResponseEntity<Void> deleteBox(@PathVariable Long userId, @PathVariable Long boxId) {
        boxService.deleteBox(userId, boxId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{boxId}/withdraw")
    public ResponseEntity<GoalBoxResponseDTO> withdraw(@PathVariable Long userId, @PathVariable Long boxId, @Valid @RequestBody GoalBoxWithdrawDTO dto) {
        GoalBox box = boxService.withdraw(userId, boxId, dto.amount());
        return ResponseEntity.ok(GoalBoxResponseDTO.from(box));
    }

    @PatchMapping("/{boxId}")
    public ResponseEntity<GoalBoxResponseDTO> updateBox(@PathVariable Long userId, @PathVariable Long boxId, @Valid @RequestBody GoalBoxUpdateDTO dto) {
        GoalBox box = boxService.updateBox(userId, boxId, dto);
        return ResponseEntity.ok(GoalBoxResponseDTO.from(box));
    }
}