package com.isaborosa.biblioteca.dto;

public record BookResponseDto(
        Long id,
        String title,
        String author,
        String isbn,
        String coverUrl,
        Integer publishYear,
        String publisher,
        Integer pageCount,
        String genre,
        String description,
        String openLibraryKey) {
}
