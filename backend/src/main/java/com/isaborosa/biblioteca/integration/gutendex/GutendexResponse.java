package com.isaborosa.biblioteca.integration.gutendex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GutendexResponse(List<Result> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(Long id, String title, List<Author> authors, Map<String, String> formats) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Author(String name) {
    }
}
