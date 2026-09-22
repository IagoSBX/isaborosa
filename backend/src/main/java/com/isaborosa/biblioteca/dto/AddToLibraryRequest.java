package com.isaborosa.biblioteca.dto;

import com.isaborosa.biblioteca.domain.userbook.ReadingStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToLibraryRequest(
        @NotNull Long bookId,
        @NotNull ReadingStatus status,
        @Min(1) @Max(5) Integer rating,
        Boolean favorite) {
}
