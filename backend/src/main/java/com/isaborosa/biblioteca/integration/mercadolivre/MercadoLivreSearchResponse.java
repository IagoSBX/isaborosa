package com.isaborosa.biblioteca.integration.mercadolivre;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadoLivreSearchResponse(List<Result> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(String title, BigDecimal price, String permalink) {
    }
}
