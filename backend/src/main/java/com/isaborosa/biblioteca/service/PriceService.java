package com.isaborosa.biblioteca.service;

import com.isaborosa.biblioteca.domain.book.Book;
import com.isaborosa.biblioteca.domain.book.BookRepository;
import com.isaborosa.biblioteca.domain.book.PriceCheck;
import com.isaborosa.biblioteca.domain.book.PriceCheckRepository;
import com.isaborosa.biblioteca.dto.PriceDto;
import com.isaborosa.biblioteca.exception.BookNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Consulta cada fonte de preco em paralelo, com timeout por fonte (a fonte
 * proprio ja tem seu timeout de HTTP; aqui garantimos um teto adicional para
 * uma fonte lenta nao travar as outras). Nunca inventa preco: fonte que falha
 * e omitida, a menos que exista um valor em cache ainda dentro da janela.
 */
@Service
public class PriceService {

    private static final long PER_SOURCE_TIMEOUT_MS = 6000;

    private final List<PriceSource> sources;
    private final BookRepository bookRepository;
    private final PriceCheckRepository priceCheckRepository;
    private final Duration cacheTtl;

    public PriceService(
            List<PriceSource> sources,
            BookRepository bookRepository,
            PriceCheckRepository priceCheckRepository,
            @Value("${app.price.cache-minutes}") long cacheMinutes) {
        this.sources = sources;
        this.bookRepository = bookRepository;
        this.priceCheckRepository = priceCheckRepository;
        this.cacheTtl = Duration.ofMinutes(cacheMinutes);
    }

    public List<PriceDto> getPrices(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));

        List<CompletableFuture<PriceDto>> futures = sources.stream()
                .map(source -> {
                    PriceDto staleFallback = priceCheckRepository.findByBookIdAndStore(book.getId(), source.storeName())
                            .map(this::toDto)
                            .orElse(null);
                    return CompletableFuture
                            .supplyAsync(() -> resolveForSource(book, source))
                            .completeOnTimeout(staleFallback, PER_SOURCE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                })
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(PriceDto::price))
                .toList();
    }

    private PriceDto resolveForSource(Book book, PriceSource source) {
        Optional<PriceCheck> cached = priceCheckRepository.findByBookIdAndStore(book.getId(), source.storeName());
        if (cached.isPresent() && isFresh(cached.get())) {
            return toDto(cached.get());
        }

        Optional<PriceQuote> quote = source.fetchPrice(book);
        if (quote.isEmpty()) {
            return cached.map(this::toDto).orElse(null);
        }

        PriceCheck saved = upsert(book, source, cached.orElse(null), quote.get());
        return toDto(saved);
    }

    private PriceCheck upsert(Book book, PriceSource source, PriceCheck existing, PriceQuote quote) {
        Instant now = Instant.now();
        if (existing != null) {
            existing.update(quote.price(), quote.url(), now);
            return priceCheckRepository.save(existing);
        }
        return priceCheckRepository.save(new PriceCheck(book, source.storeName(), quote.price(), quote.url(), now));
    }

    private boolean isFresh(PriceCheck priceCheck) {
        return Duration.between(priceCheck.getCheckedAt(), Instant.now()).compareTo(cacheTtl) < 0;
    }

    private PriceDto toDto(PriceCheck priceCheck) {
        return new PriceDto(priceCheck.getStore(), priceCheck.getPrice(), priceCheck.getUrl(), priceCheck.getCheckedAt());
    }
}
