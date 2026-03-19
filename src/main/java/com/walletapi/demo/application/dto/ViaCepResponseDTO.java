// ViaCepResponseDTO.java
package com.walletapi.demo.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ViaCepResponseDTO(

        @JsonProperty("cep")
        String cep,

        @JsonProperty("logradouro")
        String street,

        @JsonProperty("complemento")
        String complement,

        @JsonProperty("bairro")
        String neighborhood,

        @JsonProperty("localidade")
        String city,

        @JsonProperty("uf")
        String state
) {
}