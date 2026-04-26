package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.ViaCepResponseDTO;
import com.walletapi.demo.application.exceptions.CepNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViaCepServiceTest {

    @InjectMocks
    ViaCepService viaCepService;

    @Mock
    RestTemplate restTemplate;

    @Test
    @DisplayName("Should return address when CEP is valid")
    void buscarEnderecoPorCepCase1() {
        String cep = "87080078";
        String url = "https://viacep.com.br/ws/" + cep + "/json/";

        ViaCepResponseDTO mockResponse = new ViaCepResponseDTO(
                "87080-078",
                "Rua das Flores",
                "",
                "Jardim Alvorada",
                "Maringá",
                "PR"
        );

        when(restTemplate.getForObject(url, ViaCepResponseDTO.class)).thenReturn(mockResponse);

        ViaCepResponseDTO result = viaCepService.buscarEnderecoPorCep(cep);

        assertThat(result).isNotNull();
        assertThat(result.cep()).isEqualTo("87080-078");
        assertThat(result.street()).isEqualTo("Rua das Flores");
        assertThat(result.city()).isEqualTo("Maringá");
        assertThat(result.state()).isEqualTo("PR");
    }

    @Test
    @DisplayName("Should throw CepNotFoundException when API returns null")
    void buscarEnderecoPorCepCase2() {
        String cep = "00000000";
        String url = "https://viacep.com.br/ws/" + cep + "/json/";

        when(restTemplate.getForObject(url, ViaCepResponseDTO.class)).thenReturn(null);

        assertThatThrownBy(() -> viaCepService.buscarEnderecoPorCep(cep))
                .isInstanceOf(CepNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw CepNotFoundException when CEP does not exist")
    void buscarEnderecoPorCepCase3() {
        String cep = "00000000";
        String url = "https://viacep.com.br/ws/" + cep + "/json/";

        ViaCepResponseDTO invalidResponse = new ViaCepResponseDTO(null, null, null, null, null, null);

        when(restTemplate.getForObject(url, ViaCepResponseDTO.class)).thenReturn(invalidResponse);

        assertThatThrownBy(() -> viaCepService.buscarEnderecoPorCep(cep))
                .isInstanceOf(CepNotFoundException.class);
    }

    @Test
    @DisplayName("Should strip mask from CEP before calling API")
    void buscarEnderecoPorCepCase4() {
        String cepComMascara = "87080-078";
        String cepLimpo = "87080078";
        String url = "https://viacep.com.br/ws/" + cepLimpo + "/json/";

        ViaCepResponseDTO mockResponse = new ViaCepResponseDTO(
                "87080-078", "Rua das Flores", "", "Jardim Alvorada", "Maringá", "PR"
        );

        when(restTemplate.getForObject(url, ViaCepResponseDTO.class)).thenReturn(mockResponse);

        ViaCepResponseDTO result = viaCepService.buscarEnderecoPorCep(cepComMascara);

        assertThat(result).isNotNull();
        assertThat(result.cep()).isEqualTo("87080-078");
    }


    @Test
    @DisplayName("Should build full address with number and complement")
    void montarEnderecoCompletoCase1() {
        ViaCepResponseDTO endereco = new ViaCepResponseDTO(
                "87080-078", "Rua das Flores", "", "Jardim Alvorada", "Maringá", "PR"
        );

        String result = viaCepService.montarEnderecoCompleto(endereco, "123", "Apto 2");

        assertThat(result).isEqualTo("Rua das Flores, 123 - Apto 2, Jardim Alvorada, Maringá - PR, CEP: 87080-078");
    }

    @Test
    @DisplayName("Should build address with s/n when number is absent")
    void montarEnderecoCompletoCase2() {
        ViaCepResponseDTO endereco = new ViaCepResponseDTO(
                "87080-078", "Rua das Flores", "", "Jardim Alvorada", "Maringá", "PR"
        );

        String result = viaCepService.montarEnderecoCompleto(endereco, null, null);

        assertThat(result).isEqualTo("Rua das Flores, s/n, Jardim Alvorada, Maringá - PR, CEP: 87080-078");
    }

    @Test
    @DisplayName("Should build address without complement when it is absent")
    void montarEnderecoCompletoCase3() {
        ViaCepResponseDTO endereco = new ViaCepResponseDTO(
                "87080-078", "Rua das Flores", "", "Jardim Alvorada", "Maringá", "PR"
        );

        String result = viaCepService.montarEnderecoCompleto(endereco, "456", null);

        assertThat(result).isEqualTo("Rua das Flores, 456, Jardim Alvorada, Maringá - PR, CEP: 87080-078");
    }
}