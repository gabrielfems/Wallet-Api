# Wallet API

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-blue)
![JWT](https://img.shields.io/badge/Auth-JWT-black)
![JUnit 5](https://img.shields.io/badge/Tests-JUnit_5-red)
![Mockito](https://img.shields.io/badge/Mocking-Mockito-brightgreen)

API REST de carteira digital com autenticação JWT, controle de transações financeiras e gerenciamento de metas de economia.

## Sobre

Wallet API é uma Aplicação backend desenvolvida em Java com Spring Boot que permite ao usuário gerenciar sua carteira digital com depósitos, 
saques e transferências, além de criar caixinhas de metas para organizar suas economias. Cada usuário acessa e opera exclusivamente seus próprios recursos, 
garantidos por autenticação JWT e controle de ownership via Spring Security.

## Tecnologias

- Java 21
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA/Hibernate
- PostgreSQL
- SpringdocOpenAPI (Swagger UI)
- Maven


## Funcionalidades
- Cadastro e autenticação de usuários com JWT
- Senhas criptografadas com BCrypt
- Rotas protegidas com Spring Security
- Gerenciamento de carteira (saldo, status)
- Depósito, saque e transferência entre usuários
- Caixinhas de metas: criação, depósito, saque e exclusão
- Controle de ownership — cada usuário acessa apenas seus próprios dados
- Documentação interativa via Swagger UI
- Tratamento global de exceções
- Integração com a API ViaCEP

## Endpoints
 
### Auth
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/auth/register` | Cadastro de novo usuário |
| POST | `/auth/login` | Autenticação — retorna JWT |
 
### Usuário
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/users/{id}` | Busca dados do usuário |
| PATCH | `/users/{id}` | Atualiza dados do usuário |
| DELETE | `/users/{id}` | Remove o usuário |
 
### Transações
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/transactions/deposit` | Depósito na carteira |
| POST | `/transactions/withdraw` | Saque da carteira |
| POST | `/transactions/transfer` | Transferência entre usuários |
| GET | `/transactions` | Histórico de transações |
 
### Caixinhas (Metas)
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/goal-boxes` | Cria uma caixinha de meta |
| GET | `/goal-boxes` | Lista caixinhas do usuário |
| GET | `/goal-boxes/{id}` | Busca caixinha por ID |
| PATCH | `/goal-boxes/{id}` | Atualiza caixinha |
| POST | `/goal-boxes/{id}/deposit` | Deposita em uma caixinha |
| POST | `/goal-boxes/{id}/withdraw` | Saca de uma caixinha |
| DELETE | `/goal-boxes/{id}` | Remove caixinha e retorna saldo à carteira |

## Estrutura do Projeto

```
src/main/java/com/walletapi/demo/
├── application/
│   ├── controller/
│   ├── dto/
│   ├── exceptions/
│   └── service/
├── domain/
│   ├── entities/
│   └── enums/
└── infrastructure/
    ├── config/
    └── repositories/
```

## Testes

A aplicação possui testes unitários nas camadas de controller e service utilizando JUnit 5 e Mockito.

Os testes de controller foram desenvolvidos com `@WebMvcTest` e `MockMvc`, validando comportamento das rotas, autenticação e respostas HTTP.

Os testes de service utilizam `MockitoExtension`, garantindo isolamento das regras de negócio através de mocks das dependências.

### Tecnologias utilizadas
- JUnit 5
- Mockito
- MockMvc

### Executar testes

```bash
./mvnw test
```

## Como Rodar

### Pré-requisitos
- JDK 21+
- Maven
- PostgreSQL

### Passos

```bash
# 1. Clone o repositório
git clone https://github.com/gabrielfems/Wallet-Api.git

# 2. Configure o banco de dados em src/main/resources/application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/walletapi
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

# 3. Configure o secret JWT
api.security.token.secret=seu_secret

# 4. Rode a aplicação
./mvnw spring-boot:run

# 5. Todos os endpoints (exceto /auth/**) exigem o header Authorization: Bearer {token}.
Faça login em /auth/login para obter o token e utilize-o nas requisições subsequentes.
```

## Arquitetura 

O projeto segue a arquitetura em camadas:

```
Request → SecurityFilter (valida JWT) → Controller → Service → Repository → Banco de dados
```

> A validação de ownership é aplicada na camada de serviço — o token JWT identifica o usuário autenticado, que só pode operar sobre seus próprios recursos.

## Documentação da API

Documentação interativa disponível via Swagger UI em `http://localhost:8080/swagger-ui.html` com a aplicação em execução.
