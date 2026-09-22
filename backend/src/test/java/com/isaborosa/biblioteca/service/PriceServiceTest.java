package com.isaborosa.biblioteca.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.isaborosa.biblioteca.domain.book.Book;
import com.isaborosa.biblioteca.domain.book.BookRepository;
import com.isaborosa.biblioteca.domain.book.PriceCheck;
import com.isaborosa.biblioteca.domain.book.PriceCheckRepository;
import com.isaborosa.biblioteca.dto.PriceDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private PriceCheckRepository priceCheckRepository;
    @Mock
    private PriceSource workingSource;
    @Mock
    private PriceSource failingSource;

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book("O Hobbit", "J.R.R. Tolkien", "9788595084742", null, 1937, null, null, null, null, null);
        setId(book, 10L);
    }

    @Test
    void omiteFonteQueFalhaSemCacheEmVezDeInventarPreco() {
        PriceService service = new PriceService(List.of(failingSource), bookRepository, priceCheckRepository, 20);

        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(failingSource.storeName()).thenReturn("FALHA");
        when(priceCheckRepository.findByBookIdAndStore(10L, "FALHA")).thenReturn(Optional.empty());
        when(failingSource.fetchPrice(book)).thenReturn(Optional.empty());

        List<PriceDto> result = service.getPrices(10L);

        assertThat(result).isEmpty();
    }

    @Test
    void usaCacheAindaValidoSemChamarAFonteDeNovo() {
        PriceService service = new PriceService(List.of(workingSource), bookRepository, priceCheckRepository, 20);

        PriceCheck fresh = new PriceCheck(book, "LOJA", new BigDecimal("39.90"), "https://loja/produto", Instant.now());

        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(workingSource.storeName()).thenReturn("LOJA");
        when(priceCheckRepository.findByBookIdAndStore(10L, "LOJA")).thenReturn(Optional.of(fresh));

        List<PriceDto> result = service.getPrices(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).price()).isEqualByComparingTo("39.90");
    }

    @Test
    void reconsultaQuandoCacheExpirouEOrdenaPorMenorPreco() {
        PriceService service = new PriceService(List.of(workingSource), bookRepository, priceCheckRepository, 20);

        PriceCheck stale = new PriceCheck(book, "LOJA", new BigDecimal("50.00"), "https://loja/antigo",
                Instant.now().minus(1, ChronoUnit.HOURS));

        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(workingSource.storeName()).thenReturn("LOJA");
        when(priceCheckRepository.findByBookIdAndStore(10L, "LOJA")).thenReturn(Optional.of(stale));
        when(workingSource.fetchPrice(book)).thenReturn(Optional.of(new PriceQuote(new BigDecimal("42.00"), "https://loja/novo")));
        when(priceCheckRepository.save(any(PriceCheck.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<PriceDto> result = service.getPrices(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).price()).isEqualByComparingTo("42.00");
        assertThat(result.get(0).url()).isEqualTo("https://loja/novo");
    }

    private void setId(Book book, Long id) {
        try {
            var field = Book.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(book, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
