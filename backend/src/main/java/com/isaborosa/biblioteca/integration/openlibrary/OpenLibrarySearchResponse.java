package com.isaborosa.biblioteca.integration.openlibrary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibrarySearchResponse(List<Doc> docs) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Doc(
            String title,
            List<String> author_name,
            List<String> isbn,
            Integer cover_i,
            Integer first_publish_year,
            List<String> publisher,
            Integer number_of_pages_median,
            List<String> subject,
            String key) {
    }
}
