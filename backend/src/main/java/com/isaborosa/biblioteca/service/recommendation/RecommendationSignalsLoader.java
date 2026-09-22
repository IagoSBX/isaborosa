package com.isaborosa.biblioteca.service.recommendation;

import com.isaborosa.biblioteca.domain.book.Book;
import com.isaborosa.biblioteca.domain.user.UserRepository;
import com.isaborosa.biblioteca.domain.userbook.ReadingStatus;
import com.isaborosa.biblioteca.domain.userbook.UserBook;
import com.isaborosa.biblioteca.domain.userbook.UserBookRepository;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Isolado em um bean proprio (em vez de um metodo privado da estrategia) para
 * que o proxy @Transactional funcione: uma chamada interna (this.metodo())
 * nao passa pelo proxy do Spring, entao a leitura precisa vir de outro bean.
 */
@Component
class RecommendationSignalsLoader {

    private static final int MIN_RATING_FOR_AUTHOR_SIGNAL = 4;
    private static final int TOP_GENRES = 3;
    private static final int TOP_AUTHORS = 3;

    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;

    RecommendationSignalsLoader(UserBookRepository userBookRepository, UserRepository userRepository) {
        this.userBookRepository = userBookRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    RecommendationSignals load() {
        Long userId = userRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Nenhum usuario cadastrado"))
                .getId();

        List<UserBook> allUserBooks = userBookRepository.findAllByUserId(userId);

        List<UserBook> readBooks = allUserBooks.stream()
                .filter(ub -> ub.getStatus() == ReadingStatus.LIDO)
                .toList();

        // Sinal principal: generos dos livros ja lidos, do mais frequente ao
        // menos frequente (pedido explicito: priorizar pelo genero com mais
        // ocorrencias no historico de leitura).
        List<String> topGenres = topByFrequency(readBooks, ub -> ub.getBook().getGenre(), TOP_GENRES);

        // Sinal secundario: autores dos livros bem avaliados (nota >= 4),
        // como o genero sozinho pode nao capturar o gosto por um autor especifico.
        List<UserBook> wellRated = allUserBooks.stream()
                .filter(ub -> ub.getRating() != null && ub.getRating() >= MIN_RATING_FOR_AUTHOR_SIGNAL)
                .toList();
        List<String> topAuthors = topByFrequency(wellRated, ub -> ub.getBook().getAuthor(), TOP_AUTHORS);

        if (topGenres.isEmpty() && topAuthors.isEmpty() && !readBooks.isEmpty()) {
            // Nem genero nem nota alta disponiveis ainda: cai para os autores
            // do que ja foi lido, sinal mais fraco mas melhor que nada.
            topAuthors = topByFrequency(readBooks, ub -> ub.getBook().getAuthor(), TOP_AUTHORS);
        }

        return new RecommendationSignals(topGenres, topAuthors, ownedSignatures(allUserBooks));
    }

    private List<String> topByFrequency(
            List<UserBook> books, java.util.function.Function<UserBook, String> keyExtractor, int limit) {
        Map<String, Long> frequency = new LinkedHashMap<>();
        for (UserBook userBook : books) {
            String key = keyExtractor.apply(userBook);
            if (!StringUtils.hasText(key)) {
                continue;
            }
            frequency.merge(key, 1L, Long::sum);
        }
        return frequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    private Set<String> ownedSignatures(List<UserBook> allUserBooks) {
        Set<String> signatures = new HashSet<>();
        for (UserBook userBook : allUserBooks) {
            Book book = userBook.getBook();
            signatures.add(RecommendationSignatures.of(book.getIsbn(), book.getOpenLibraryKey(), book.getTitle()));
        }
        return signatures;
    }
}
