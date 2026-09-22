package com.isaborosa.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBookRequest(
        @NotBlank @Size(max = 500) String title,
        @Size(max = 300) String author,
        @Size(max = 20) String isbn,
        @Size(max = 500) String coverUrl,
        Integer publishYear,
        @Size(max = 200) String publisher,
        Integer pageCount,
        @Size(max = 100) String genre,
        String description,
        @Size(max = 50) String openLibraryKey) {
}
