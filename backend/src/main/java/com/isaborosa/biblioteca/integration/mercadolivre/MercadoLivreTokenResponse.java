package com.isaborosa.biblioteca.integration.mercadolivre;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadoLivreTokenResponse(String access_token, Integer expires_in) {
}
