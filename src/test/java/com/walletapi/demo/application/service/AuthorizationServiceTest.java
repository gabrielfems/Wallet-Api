package com.walletapi.demo.application.service;

import com.walletapi.demo.domain.entities.UserCredentials;
import com.walletapi.demo.domain.enums.UserRole;
import com.walletapi.demo.infrastructure.repositories.UserCredentialsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @InjectMocks
    private AuthorizationService authorizationService;

    @Mock
    private UserCredentialsRepository repository;

    private UserCredentials credentials;

    @BeforeEach
    void setUp() {
        credentials = new UserCredentials();
        credentials.setId("uuid-teste");
        credentials.setLogin("gabriel@email.com");
        credentials.setPassword("hash");
        credentials.setRole(UserRole.USER);
    }

    @Test
    @DisplayName("loadUserByUsername: deve retornar as credenciais quando o login existe")
    void loadUserByUsername_loginExiste_retornaCredenciais() {
        when(repository.findByLogin("gabriel@email.com")).thenReturn(credentials);

        UserDetails result = authorizationService.loadUserByUsername("gabriel@email.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("gabriel@email.com");
        verify(repository).findByLogin("gabriel@email.com");
    }

    @Test
    @DisplayName("loadUserByUsername: deve retornar null quando o login não existe")
    void loadUserByUsername_loginNaoExiste_retornaNull() {
        when(repository.findByLogin("inexistente@email.com")).thenReturn(null);

        UserDetails result = authorizationService.loadUserByUsername("inexistente@email.com");

        assertThat(result).isNull();
        verify(repository).findByLogin("inexistente@email.com");
    }
}