package com.isaborosa.biblioteca.dto;

public record BookSearchResultDto(
        String title,
        String author,
        String isbn,
        String coverUrl,
        Integer publishYear,
        String publisher,
        Integer pageCount,
        String genre,
        String openLibraryKey) {
}
