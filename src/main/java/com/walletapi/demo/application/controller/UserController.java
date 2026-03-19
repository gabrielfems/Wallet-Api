package com.walletapi.demo.application.controller;

import com.walletapi.demo.application.dto.UserCreateDTO;
import com.walletapi.demo.application.dto.UserUpdateDTO;
import com.walletapi.demo.application.dto.UserResponseDTO;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.infrastructure.repositories.UserRepository;
import com.walletapi.demo.application.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO dto) {
        return new ResponseEntity<>(UserResponseDTO.from(userService.createUser(dto)), HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        User newUpdate = userService.updateUser(id, dto);
        return ResponseEntity.ok(UserResponseDTO.from(newUpdate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
