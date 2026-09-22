package com.isaborosa.biblioteca.dto;

import com.isaborosa.biblioteca.domain.userbook.ReadingStatus;
import java.util.List;
import java.util.Map;

public record StatsDto(
        long totalBooks,
        Map<ReadingStatus, Long> booksByStatus,
        Long totalPagesRead,
        Double averageRating,
        String topGenre,
        List<GenreCountDto> genreDistribution,
        List<RatingCountDto> ratingDistribution,
        List<MonthCountDto> readingEvolution) {

    public record GenreCountDto(String genre, long count) {
    }

    public record RatingCountDto(int rating, long count) {
    }

    public record MonthCountDto(String month, long count) {
    }
}
