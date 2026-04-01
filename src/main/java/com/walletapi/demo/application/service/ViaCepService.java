package com.walletapi.demo.application.service;

import com.walletapi.demo.application.dto.ViaCepResponseDTO;
import com.walletapi.demo.application.exceptions.CepNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ViaCepService {

    private final RestTemplate restTemplate;

    public ViaCepService() {
        this.restTemplate = new RestTemplate();
    }

    public ViaCepResponseDTO buscarEnderecoPorCep(String cep) {
        String cepLimpo = cep.replaceAll("[^0-9]", "");
        String url = "https://viacep.com.br/ws/" + cepLimpo + "/json/";

        ViaCepResponseDTO response = restTemplate.getForObject(url, ViaCepResponseDTO.class);

        if (response == null || response.cep() == null) {
            throw new CepNotFoundException(cep);
        }

        return response;
    }

    public String montarEnderecoCompleto(ViaCepResponseDTO endereco, String numero, String complemento) {
        StringBuilder address = new StringBuilder();

        // Logradouro
        if (endereco.street() != null) {
            address.append(endereco.street());
        }

        // Número
        if (numero != null && !numero.isEmpty()) {
            address.append(", ").append(numero);
        } else {
            address.append(", s/n");
        }

        // Complemento
        if (complemento != null && !complemento.isEmpty()) {
            address.append(" - ").append(complemento);
        }

        // Bairro
        if (endereco.neighborhood() != null) {
            address.append(", ").append(endereco.neighborhood());
        }

        // Cidade/UF
        address.append(", ").append(endereco.city()).append(" - ").append(endereco.state());

        // CEP
        address.append(", CEP: ").append(endereco.cep());

        return address.toString();
    }

}