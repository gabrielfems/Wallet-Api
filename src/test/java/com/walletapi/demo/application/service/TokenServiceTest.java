package com.walletapi.demo.application.service;

import com.walletapi.demo.domain.entities.UserCredentials;
import com.walletapi.demo.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = TokenService.class)
@TestPropertySource(properties = "api.security.token.secret=test-secret-key-for-unit-tests")
class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

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
    @DisplayName("generateToken: deve gerar um token não nulo e não vazio para credenciais válidas")
    void generateToken_credenciaisValidas_retornaTokenNaoVazio() {
        String token = tokenService.generateToken(credentials);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("generateToken: deve gerar um token JWT com três partes separadas por ponto")
    void generateToken_credenciaisValidas_retornaTokenNoFormatoJWT() {
        String token = tokenService.generateToken(credentials);

        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
    }

    @Test
    @DisplayName("validateToken: deve retornar o login do usuário quando o token é válido")
    void validateToken_tokenValido_retornaLogin() {
        String token = tokenService.generateToken(credentials);

        String login = tokenService.validateToken(token);

        assertThat(login).isEqualTo("gabriel@email.com");
    }

    @Test
    @DisplayName("validateToken: deve retornar string vazia quando o token é inválido")
    void validateToken_tokenInvalido_retornaStringVazia() {
        String login = tokenService.validateToken("token.invalido.qualquer");

        assertThat(login).isEmpty();
    }

    @Test
    @DisplayName("validateToken: deve retornar string vazia quando o token foi assinado com secret diferente")
    void validateToken_tokenComSecretDiferente_retornaStringVazia() {
        String tokenDeOutroServidor = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
                ".eyJzdWIiOiJoYWNrZXJAZW1haWwuY29tIiwiaXNzIjoiYXV0aC1hcGkifQ" +
                ".assinatura-invalida-com-outro-secret";

        String login = tokenService.validateToken(tokenDeOutroServidor);

        assertThat(login).isEmpty();
    }

    @Test
    @DisplayName("validateToken: deve retornar string vazia quando o token é string vazia")
    void validateToken_tokenVazio_retornaStringVazia() {
        String login = tokenService.validateToken("");

        assertThat(login).isEmpty();
    }

    @Test
    @DisplayName("generateToken + validateToken: token gerado deve ser validado corretamente pelo mesmo service")
    void gerarEValidarToken_cicloCompleto_loginBate() {
        String token = tokenService.generateToken(credentials);
        String loginRecuperado = tokenService.validateToken(token);

        assertThat(loginRecuperado).isEqualTo(credentials.getLogin());
    }
}