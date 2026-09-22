package com.isaborosa.biblioteca.service;

import com.isaborosa.biblioteca.domain.book.Book;
import com.isaborosa.biblioteca.domain.user.UserRepository;
import com.isaborosa.biblioteca.domain.userbook.ReadingStatus;
import com.isaborosa.biblioteca.domain.userbook.UserBook;
import com.isaborosa.biblioteca.domain.userbook.UserBookRepository;
import com.isaborosa.biblioteca.dto.StatsDto;
import com.isaborosa.biblioteca.dto.StatsDto.GenreCountDto;
import com.isaborosa.biblioteca.dto.StatsDto.MonthCountDto;
import com.isaborosa.biblioteca.dto.StatsDto.RatingCountDto;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Agregacoes simples sobre a biblioteca pessoal para a tela de estatisticas
 * (livros por status, paginas lidas, nota media, distribuicao de generos e
 * de avaliacoes, evolucao de leitura por mes).
 */
@Service
public class StatsService {

    private static final int EVOLUTION_MONTHS = 12;
    private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy-MM");

    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;

    public StatsService(UserBookRepository userBookRepository, UserRepository userRepository) {
        this.userBookRepository = userBookRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public StatsDto getStats() {
        Long userId = userRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Nenhum usuario cadastrado"))
                .getId();

        List<UserBook> all = userBookRepository.findAllByUserId(userId);

        Map<ReadingStatus, Long> booksByStatus = new LinkedHashMap<>();
        for (ReadingStatus status : ReadingStatus.values()) {
            long count = all.stream().filter(ub -> ub.getStatus() == status).count();
            booksByStatus.put(status, count);
        }

        Long totalPagesRead = all.stream()
                .filter(ub -> ub.getStatus() == ReadingStatus.LIDO)
                .map(ub -> ub.getBook().getPageCount())
                .filter(pageCount -> pageCount != null)
                .mapToLong(Integer::longValue)
                .boxed()
                .reduce(Long::sum)
                .orElse(null);

        OptionalDouble averageRatingOptional = all.stream()
                .map(UserBook::getRating)
                .filter(rating -> rating != null)
                .mapToInt(Integer::intValue)
                .average();
        Double averageRating = averageRatingOptional.isPresent() ? averageRatingOptional.getAsDouble() : null;

        List<GenreCountDto> genreDistribution = all.stream()
                .map(UserBook::getBook)
                .map(Book::getGenre)
                .filter(StringUtils::hasText)
                .collect(Collectors.groupingBy(genre -> genre, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> new GenreCountDto(entry.getKey(), entry.getValue()))
                .toList();

        String topGenre = genreDistribution.isEmpty() ? null : genreDistribution.get(0).genre();

        List<RatingCountDto> ratingDistribution = ratingDistribution(all);
        List<MonthCountDto> readingEvolution = readingEvolution(all);

        return new StatsDto(all.size(), booksByStatus, totalPagesRead, averageRating, topGenre,
                genreDistribution, ratingDistribution, readingEvolution);
    }

    private List<RatingCountDto> ratingDistribution(List<UserBook> all) {
        Map<Integer, Long> counts = all.stream()
                .map(UserBook::getRating)
                .filter(rating -> rating != null)
                .collect(Collectors.groupingBy(rating -> rating, Collectors.counting()));

        return IntStream.rangeClosed(1, 5)
                .mapToObj(rating -> new RatingCountDto(rating, counts.getOrDefault(rating, 0L)))
                .toList();
    }

    /**
     * Aproxima "quando o livro foi lido" pelo mes da ultima atualizacao do
     * registro (updatedAt) para itens com status "ja li" - o escopo fechado
     * do projeto nao guarda uma data de leitura dedicada, entao usamos o
     * melhor dado real que ja existe em vez de inventar uma data.
     */
    private List<MonthCountDto> readingEvolution(List<UserBook> all) {
        Map<YearMonth, Long> countsByMonth = all.stream()
                .filter(ub -> ub.getStatus() == ReadingStatus.LIDO && ub.getUpdatedAt() != null)
                .map(ub -> YearMonth.from(ub.getUpdatedAt().atZone(ZoneOffset.UTC)))
                .collect(Collectors.groupingBy(month -> month, Collectors.counting()));

        if (countsByMonth.isEmpty()) {
            return List.of();
        }

        YearMonth earliest = countsByMonth.keySet().stream().min(YearMonth::compareTo).orElseThrow();
        YearMonth latest = countsByMonth.keySet().stream().max(YearMonth::compareTo).orElseThrow();
        YearMonth start = latest.minusMonths(EVOLUTION_MONTHS - 1L);
        if (start.isBefore(earliest)) {
            start = earliest;
        }

        List<MonthCountDto> evolution = new ArrayList<>();
        for (YearMonth month = start; !month.isAfter(latest); month = month.plusMonths(1)) {
            evolution.add(new MonthCountDto(month.format(MONTH_KEY), countsByMonth.getOrDefault(month, 0L)));
        }
        return evolution;
    }
}
