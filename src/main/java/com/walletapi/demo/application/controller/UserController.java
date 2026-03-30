package com.walletapi.demo.application.controller;

import com.walletapi.demo.application.dto.UserCreateDTO;
import com.walletapi.demo.application.dto.UserResponseDTO;
import com.walletapi.demo.application.dto.UserUpdateDTO;
import com.walletapi.demo.application.service.UserService;
import com.walletapi.demo.domain.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou dados obrigatórios ausentes")
    @ApiResponse(responseCode = "404", description = "CEP não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO dto) {
        return new ResponseEntity<>(UserResponseDTO.from(userService.createUser(dto)), HttpStatus.CREATED);
    }

    @GetMapping("/list")
    @Operation(summary= "Listar usuários", description= "Lista todos os usuários cadastrados")
    @ApiResponse(responseCode = "200", description = "Usuários encontrados com sucesso")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PatchMapping("/{id}")
    @Operation(summary= "Atualizar usuário", description= "Método para atualizar os dados de um usuário")
    @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou dados obrigatórios ausentes")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        User newUpdate = userService.updateUser(id, dto);
        return ResponseEntity.ok(UserResponseDTO.from(newUpdate));
    }

    @DeleteMapping("/{id}")
    @Operation(summary= "Deletar usuário", description= "Remove um usuário da base de dados")
    @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
