package com.isaborosa.biblioteca.integration.googlebooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleBooksResponse(List<Item> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(String id, VolumeInfo volumeInfo) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VolumeInfo(
            String title,
            List<String> authors,
            String publisher,
            String publishedDate,
            Integer pageCount,
            List<String> categories,
            ImageLinks imageLinks,
            List<IndustryIdentifier> industryIdentifiers) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ImageLinks(String thumbnail, String smallThumbnail) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IndustryIdentifier(String type, String identifier) {
    }
}
