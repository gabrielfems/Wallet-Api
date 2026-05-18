package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.ViaCepResponseDTO;
import com.walletapi.demo.application.exceptions.CepNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViaCepServiceTest {

    @InjectMocks
    private ViaCepService viaCepService;

    @Mock
    private RestTemplate restTemplate;

    private ViaCepResponseDTO enderecoValido;

    @BeforeEach
    void setUp() {
        enderecoValido = new ViaCepResponseDTO(
                "87020-900",
                "Avenida Brasil",
                "",
                "Zona 01",
                "Maringá",
                "PR"
        );
    }

    @Test
    @DisplayName("buscarEnderecoPorCep: deve retornar o endereço quando CEP é válido")
    void buscarEnderecoPorCep_cepValido_retornaEndereco() {
        when(restTemplate.getForObject(
                "https://viacep.com.br/ws/87020900/json/",
                ViaCepResponseDTO.class)
        ).thenReturn(enderecoValido);

        ViaCepResponseDTO result = viaCepService.buscarEnderecoPorCep("87020-900");

        assertThat(result).isNotNull();
        assertThat(result.cep()).isEqualTo("87020-900");
        assertThat(result.city()).isEqualTo("Maringá");
        verify(restTemplate).getForObject("https://viacep.com.br/ws/87020900/json/", ViaCepResponseDTO.class);
    }

    @Test
    @DisplayName("buscarEnderecoPorCep: deve limpar máscara do CEP antes de montar a URL")
    void buscarEnderecoPorCep_cepComMascara_removeMascaraNaUrl() {
        when(restTemplate.getForObject(
                "https://viacep.com.br/ws/87020900/json/",
                ViaCepResponseDTO.class)
        ).thenReturn(enderecoValido);

        viaCepService.buscarEnderecoPorCep("87020-900");

        verify(restTemplate).getForObject("https://viacep.com.br/ws/87020900/json/", ViaCepResponseDTO.class);
        verify(restTemplate, never()).getForObject(contains("87020-900"), eq(ViaCepResponseDTO.class));
    }

    @Test
    @DisplayName("buscarEnderecoPorCep: deve lançar CepNotFoundException quando resposta é nula")
    void buscarEnderecoPorCep_respostaNula_lancaCepNotFoundException() {
        when(restTemplate.getForObject(anyString(), eq(ViaCepResponseDTO.class))).thenReturn(null);

        assertThatThrownBy(() -> viaCepService.buscarEnderecoPorCep("00000000"))
                .isInstanceOf(CepNotFoundException.class);
    }

    @Test
    @DisplayName("buscarEnderecoPorCep: deve lançar CepNotFoundException quando CEP não existe na base do ViaCEP")
    void buscarEnderecoPorCep_cepInexistente_lancaCepNotFoundException() {
        ViaCepResponseDTO respostaInvalida = new ViaCepResponseDTO(null, null, null, null, null, null);

        when(restTemplate.getForObject(anyString(), eq(ViaCepResponseDTO.class))).thenReturn(respostaInvalida);

        assertThatThrownBy(() -> viaCepService.buscarEnderecoPorCep("00000000"))
                .isInstanceOf(CepNotFoundException.class);
    }

    @Test
    @DisplayName("montarEnderecoCompleto: deve montar endereço completo com todos os campos")
    void montarEnderecoCompleto_todosOsCampos_retornaEnderecoCompleto() {
        String result = viaCepService.montarEnderecoCompleto(enderecoValido, "123", "Apto 4");

        assertThat(result).isEqualTo("Avenida Brasil, 123 - Apto 4, Zona 01, Maringá - PR");
    }

    @Test
    @DisplayName("montarEnderecoCompleto: deve usar s/n quando número não é informado")
    void montarEnderecoCompleto_semNumero_usaSN() {
        String result = viaCepService.montarEnderecoCompleto(enderecoValido, null, null);

        assertThat(result).isEqualTo("Avenida Brasil, s/n, Zona 01, Maringá - PR");
    }

    @Test
    @DisplayName("montarEnderecoCompleto: deve omitir complemento quando não é informado")
    void montarEnderecoCompleto_semComplemento_omiteComplemento() {
        String result = viaCepService.montarEnderecoCompleto(enderecoValido, "456", null);

        assertThat(result).isEqualTo("Avenida Brasil, 456, Zona 01, Maringá - PR");
        assertThat(result).doesNotContain("Apto");
    }

    @Test
    @DisplayName("montarEnderecoCompleto: deve omitir complemento quando é string vazia")
    void montarEnderecoCompleto_complementoVazio_omiteComplemento() {
        String result = viaCepService.montarEnderecoCompleto(enderecoValido, "456", "");

        assertThat(result).isEqualTo("Avenida Brasil, 456, Zona 01, Maringá - PR");
    }

    @Test
    @DisplayName("montarEnderecoCompleto: deve montar endereço sem logradouro quando campo é nulo")
    void montarEnderecoCompleto_semLogradouro_omiteLogradouro() {
        ViaCepResponseDTO semLogradouro = new ViaCepResponseDTO(
                "87020-900", null, null, "Zona 01", "Maringá", "PR"
        );

        String result = viaCepService.montarEnderecoCompleto(semLogradouro, "123", null);

        assertThat(result).isEqualTo(", 123, Zona 01, Maringá - PR");
    }
}