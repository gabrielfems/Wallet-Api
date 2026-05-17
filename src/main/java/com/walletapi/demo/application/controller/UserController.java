package com.walletapi.demo.application.controller;

import com.walletapi.demo.application.dto.UserResponseDTO;
import com.walletapi.demo.application.dto.UserUpdateDTO;
import com.walletapi.demo.application.service.UserService;
import com.walletapi.demo.domain.entities.User;
import com.walletapi.demo.domain.entities.UserCredentials;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@Tag(name= "Users", description= "Gereciador de usuários")
public class UserController {

    private final UserService userService;

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
    @ApiResponse(responseCode = "422", description = "Usuário não tem permissão para atualizar essa conta")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id,
                                                      @Valid @RequestBody UserUpdateDTO dto,
                                                      Authentication authentication) {

        UserCredentials credentials = (UserCredentials) authentication.getPrincipal();
        Long authenticatedUserId = credentials.getUser().getId();
        boolean isAdmin = credentials.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !authenticatedUserId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User newUpdate = userService.updateUser(id, dto);
        return ResponseEntity.ok(UserResponseDTO.from(newUpdate));
    }

    @DeleteMapping("/{id}")
    @Operation(summary= "Deletar usuário", description= "Remove um usuário da base de dados")
    @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "422", description = "Usuário não tem permissão para deletar essa conta")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           Authentication authentication) {

        UserCredentials credentials = (UserCredentials) authentication.getPrincipal();
        Long authenticatedUserId = credentials.getUser().getId();
        boolean isAdmin = credentials.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !authenticatedUserId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}