package com.walletapi.demo.application.controller;

import com.walletapi.demo.application.dto.AuthenticationDTO;
import com.walletapi.demo.application.dto.LoginResponseDTO;
import com.walletapi.demo.application.dto.UserRegisterDTO;
import com.walletapi.demo.application.service.TokenService;
import com.walletapi.demo.application.service.UserService;
import com.walletapi.demo.domain.entities.UserCredentials;
import com.walletapi.demo.infrastructure.repositories.UserCredentialsRepository;
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
    public ResponseEntity login(@RequestBody @Valid AuthenticationDTO data){
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
        var auth = authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((UserCredentials) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid UserRegisterDTO data){
        if (this.repository.findByLogin(data.login()) != null) return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        UserCredentials newUserCred = new UserCredentials(data.login(), encryptedPassword, data.role());

        userService.createUser(data);

        this.repository.save(newUserCred);
        return ResponseEntity.ok().build();
    }
}
