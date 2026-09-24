package com.isaborosa.biblioteca.service;

import com.isaborosa.biblioteca.domain.book.Book;
import com.isaborosa.biblioteca.domain.book.BookRepository;
import com.isaborosa.biblioteca.dto.FreeSourceDto;
import com.isaborosa.biblioteca.exception.BookNotFoundException;
import com.isaborosa.biblioteca.integration.gutendex.GutendexClient;
import com.isaborosa.biblioteca.integration.openlibrary.OpenLibraryClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Procura fontes legais e gratuitas para ler o livro (Project Gutenberg e
 * Internet Archive). Nunca sugere pirataria; se nenhuma fonte confirmar
 * disponibilidade real, devolve lista vazia em vez de inventar um link.
 */
@Service
public class FreeSourceService {

    private final BookRepository bookRepository;
    private final GutendexClient gutendexClient;
    private final OpenLibraryClient openLibraryClient;

    public FreeSourceService(
            BookRepository bookRepository, GutendexClient gutendexClient, OpenLibraryClient openLibraryClient) {
        this.bookRepository = bookRepository;
        this.gutendexClient = gutendexClient;
        this.openLibraryClient = openLibraryClient;
    }

    @Transactional(readOnly = true)
    public List<FreeSourceDto> getFreeSources(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
        String title = book.getTitle();
        String author = book.getAuthor();
        String isbn = book.getIsbn();

        CompletableFuture<Optional<FreeSourceDto>> gutendex =
                CompletableFuture.supplyAsync(() -> gutendexClient.findFreeBook(title, author));
        CompletableFuture<Optional<FreeSourceDto>> internetArchive =
                CompletableFuture.supplyAsync(() -> openLibraryClient.fetchInternetArchiveSource(isbn, title, author));

        List<FreeSourceDto> sources = new ArrayList<>();
        gutendex.join().ifPresent(sources::add);
        internetArchive.join().ifPresent(sources::add);
        return sources;
    }
}
