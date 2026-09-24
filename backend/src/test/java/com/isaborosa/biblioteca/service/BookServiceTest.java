package com.isaborosa.biblioteca.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.isaborosa.biblioteca.domain.book.Book;
import com.isaborosa.biblioteca.domain.book.BookRepository;
import com.isaborosa.biblioteca.integration.googlebooks.GoogleBooksClient;
import com.isaborosa.biblioteca.integration.openlibrary.OpenLibraryClient;
import com.isaborosa.biblioteca.integration.translation.TranslationClient;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private OpenLibraryClient openLibraryClient;

    @Mock
    private GoogleBooksClient googleBooksClient;

    @Mock
    private TranslationClient translationClient;

    private BookService bookService() {
        return new BookService(bookRepository, openLibraryClient, googleBooksClient, translationClient);
    }

    @Test
    void traduzSinopseEmInglesEPreservaFichaTecnica() {
        Book book = new Book(
                "Pride and Prejudice",
                "Jane Austen",
                null,
                null,
                1813,
                null,
                432,
                "Romance",
                "A story about love and misunderstanding.\n\nFicha técnica: 432 páginas · Romance.",
                "/works/OL66554W");
        when(bookRepository.findByOpenLibraryKeyStartingWithAndDescriptionIsNotNull("/works/"))
                .thenReturn(List.of(book));
        when(translationClient.translateToPortuguese("A story about love and misunderstanding."))
                .thenReturn(Optional.of("Uma história sobre amor e mal-entendidos."));

        int updated = bookService().retranslatePendingDescriptions();

        assertThat(updated).isEqualTo(1);
        assertThat(book.getDescription())
                .isEqualTo("Uma história sobre amor e mal-entendidos.\n\nFicha técnica: 432 páginas · Romance.");
    }

    @Test
    void naoRetraduzSinopseQueJaParecePortuguesa() {
        Book book = new Book(
                "Orgulho e Preconceito",
                "Jane Austen",
                null,
                null,
                1813,
                null,
                432,
                "Romance",
                "Uma história que fala sobre o amor.",
                "/works/OL66554W");
        when(bookRepository.findByOpenLibraryKeyStartingWithAndDescriptionIsNotNull("/works/"))
                .thenReturn(List.of(book));

        int updated = bookService().retranslatePendingDescriptions();

        assertThat(updated).isZero();
        verify(translationClient, never()).translateToPortuguese(any());
    }

    @Test
    void traduzSinopseSemFichaTecnica() {
        Book book = new Book(
                "A Tale",
                "Some Author",
                null,
                null,
                null,
                null,
                null,
                null,
                "This is a short synopsis without extra facts.",
                "/works/OL1W");
        when(bookRepository.findByOpenLibraryKeyStartingWithAndDescriptionIsNotNull("/works/"))
                .thenReturn(List.of(book));
        when(translationClient.translateToPortuguese("This is a short synopsis without extra facts."))
                .thenReturn(Optional.of("Esta é uma sinopse curta sem fatos extras."));

        int updated = bookService().retranslatePendingDescriptions();

        assertThat(updated).isEqualTo(1);
        assertThat(book.getDescription()).isEqualTo("Esta é uma sinopse curta sem fatos extras.");
    }
}
