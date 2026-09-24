package com.isaborosa.biblioteca.integration.translation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MyMemoryResponse(ResponseData responseData) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResponseData(String translatedText) {
    }
}
