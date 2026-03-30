package com.walletapi.demo.application.controller;

import com.walletapi.demo.application.dto.UserCreateDTO;
import com.walletapi.demo.application.dto.UserResponseDTO;
import com.walletapi.demo.application.dto.UserUpdateDTO;
import com.walletapi.demo.application.service.UserService;
import com.walletapi.demo.domain.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController()
@RequestMapping("/api/users")
@Tag(name= "Users", description= "Gereciador de usuários")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    @Operation(summary= "Cadastrar usuário", description= "Cadastra um novo usuário na base de dados")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO dto) {
        return new ResponseEntity<>(UserResponseDTO.from(userService.createUser(dto)), HttpStatus.CREATED);
    }

    @GetMapping("/list")
    @Operation(summary= "Listar usuários", description= "Lista todos os usuários cadastrados")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PatchMapping("/{id}")
    @Operation(summary= "Atualizar usuário", description= "Atualiza os dados do usuário")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        User newUpdate = userService.updateUser(id, dto);
        return ResponseEntity.ok(UserResponseDTO.from(newUpdate));
    }

    @DeleteMapping("/{id}")
    @Operation(summary= "Deletar usuário", description= "Remove um usuário da base de dados")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
