package com.isaborosa.biblioteca.dto;

import com.isaborosa.biblioteca.domain.userbook.ReadingStatus;
import java.time.Instant;

public record UserBookResponseDto(
        Long id,
        BookResponseDto book,
        ReadingStatus status,
        Integer rating,
        boolean favorite,
        Integer currentPage,
        Instant createdAt,
        Instant updatedAt) {
}
