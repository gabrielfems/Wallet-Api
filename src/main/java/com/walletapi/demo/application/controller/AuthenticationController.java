package com.walletapi.demo.application.controller;

import com.walletapi.demo.application.dto.AuthenticationDTO;
import com.walletapi.demo.application.dto.LoginResponseDTO;
import com.walletapi.demo.application.dto.UserRegisterDTO;
import com.walletapi.demo.application.service.TokenService;
import com.walletapi.demo.application.service.UserService;
import com.walletapi.demo.domain.entities.UserCredentials;
import com.walletapi.demo.infrastructure.repositories.UserCredentialsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserCredentialsRepository repository;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    @Operation(summary= "Login", description= "Autentica o usuário e retorna um token JWT")
    @ApiResponse(responseCode= "200", description = "Autenticado com sucesso")
    @ApiResponse(responseCode= "400", description = "Requisição inválida")
    @ApiResponse(responseCode= "403", description = "Acesso negado")
    public ResponseEntity login(@RequestBody @Valid AuthenticationDTO data){
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
        var auth = authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((UserCredentials) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    @Operation(summary= "Registro", description= "Cria um novo usuário na base de dados")
    @ApiResponse(responseCode= "200", description = "Usuário criado com sucesso")
    @ApiResponse(responseCode= "400", description = "Usuário já cadastrado ou dados inválidos")
    public ResponseEntity register(@RequestBody @Valid UserRegisterDTO data){
        if (this.repository.findByLogin(data.login()) != null) return ResponseEntity.badRequest().build();

        userService.createUser(data);

        return ResponseEntity.ok().build();
    }
}
