package com.isaborosa.biblioteca.integration.openlibrary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibraryTrendingResponse(List<Work> works) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Work(
            String title,
            List<String> author_name,
            Integer cover_i,
            Integer first_publish_year,
            String key) {
    }
}
