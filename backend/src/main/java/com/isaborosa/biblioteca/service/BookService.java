package com.isaborosa.biblioteca.service;

import com.isaborosa.biblioteca.domain.book.Book;
import com.isaborosa.biblioteca.domain.book.BookRepository;
import com.isaborosa.biblioteca.dto.BookResponseDto;
import com.isaborosa.biblioteca.dto.BookSearchResultDto;
import com.isaborosa.biblioteca.dto.CreateBookRequest;
import com.isaborosa.biblioteca.exception.BookNotFoundException;
import com.isaborosa.biblioteca.integration.googlebooks.GoogleBooksClient;
import com.isaborosa.biblioteca.integration.openlibrary.OpenLibraryClient;
import com.isaborosa.biblioteca.integration.translation.TranslationClient;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BookService {

    private static final int RESULTS_PER_PAGE = 20;

    private static final String FICHA_TECNICA_MARKER = "\n\nFicha técnica:";

    /**
     * Palavras que praticamente nunca aparecem em texto em ingles com esses
     * espacos ao redor - usadas para nao re-traduzir (e arriscar degradar)
     * uma sinopse que ja esta em portugues.
     */
    private static final String[] PORTUGUESE_MARKERS = {" que ", " não ", " para ", " são ", " está ", " uma "};

    private final BookRepository bookRepository;
    private final OpenLibraryClient openLibraryClient;
    private final GoogleBooksClient googleBooksClient;
    private final TranslationClient translationClient;

    public BookService(BookRepository bookRepository, OpenLibraryClient openLibraryClient,
            GoogleBooksClient googleBooksClient, TranslationClient translationClient) {
        this.bookRepository = bookRepository;
        this.openLibraryClient = openLibraryClient;
        this.googleBooksClient = googleBooksClient;
        this.translationClient = translationClient;
    }

    /**
     * Combina Open Library (fonte principal, sempre disponivel) com Google
     * Books (fonte extra, so quando configurada) para a mesma pagina, remove
     * duplicatas entre as duas (por ISBN, ou por titulo+autor quando nao ha
     * ISBN) e devolve uma lista unica. Paginado por offset para permitir
     * scroll infinito no frontend sem carregar tudo de uma vez.
     */
    public List<BookSearchResultDto> popular() {
        return openLibraryClient.fetchTrending(24);
    }

    public List<BookSearchResultDto> search(String query, int page) {
        int offset = page * RESULTS_PER_PAGE;
        List<BookSearchResultDto> fromOpenLibrary = openLibraryClient.search(query, offset, RESULTS_PER_PAGE);
        List<BookSearchResultDto> fromGoogleBooks = googleBooksClient.isConfigured()
                ? googleBooksClient.search(query, offset, RESULTS_PER_PAGE)
                : List.of();
        return mergeDeduped(fromOpenLibrary, fromGoogleBooks);
    }

    /**
     * Busca por genero (subject), usada para navegar por categoria em vez de
     * texto livre - mais precisa que jogar o nome do genero em "q".
     */
    public List<BookSearchResultDto> searchByGenre(String genre, int page) {
        int offset = page * RESULTS_PER_PAGE;
        List<BookSearchResultDto> fromOpenLibrary = openLibraryClient.searchByGenre(genre, offset, RESULTS_PER_PAGE);
        List<BookSearchResultDto> fromGoogleBooks = googleBooksClient.isConfigured()
                ? googleBooksClient.search("subject:" + genre, offset, RESULTS_PER_PAGE)
                : List.of();
        return mergeDeduped(fromOpenLibrary, fromGoogleBooks);
    }

    private List<BookSearchResultDto> mergeDeduped(List<BookSearchResultDto> primary, List<BookSearchResultDto> secondary) {
        Map<String, BookSearchResultDto> merged = new LinkedHashMap<>();
        for (BookSearchResultDto result : primary) {
            merged.putIfAbsent(dedupeSignature(result), result);
        }
        for (BookSearchResultDto result : secondary) {
            merged.putIfAbsent(dedupeSignature(result), result);
        }
        return List.copyOf(merged.values());
    }

    private String dedupeSignature(BookSearchResultDto result) {
        if (StringUtils.hasText(result.isbn())) {
            return "isbn:" + result.isbn();
        }
        String title = result.title() == null ? "" : result.title().trim().toLowerCase();
        String author = result.author() == null ? "" : result.author().trim().toLowerCase();
        return "ta:" + title + "|" + author;
    }

    @Transactional(readOnly = true)
    public BookResponseDto getById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        return toDto(book);
    }

    @Transactional
    public BookResponseDto upsert(CreateBookRequest request) {
        Book incoming = new Book(
                request.title(),
                request.author(),
                request.isbn(),
                request.coverUrl(),
                request.publishYear(),
                request.publisher(),
                request.pageCount(),
                request.genre(),
                buildDescription(request),
                request.openLibraryKey());

        Book existing = findExisting(request).orElse(null);
        if (existing != null) {
            existing.updateFrom(incoming);
            return toDto(existing);
        }
        return toDto(bookRepository.save(incoming));
    }

    /**
     * Compoe uma descricao completa: a sinopse real da Open Library (quando
     * existe) seguida de uma ficha tecnica com os dados que ja temos (paginas,
     * genero, ano, editora). Se a sinopse nao existir, a ficha tecnica sozinha
     * ja garante que a descricao nunca fica vazia com dados reais disponiveis.
     * Uma descricao explicita no request (ex.: edicao manual futura) nunca e
     * sobrescrita.
     */
    private String buildDescription(CreateBookRequest request) {
        if (StringUtils.hasText(request.description())) {
            return request.description();
        }

        boolean isRealOpenLibraryKey = StringUtils.hasText(request.openLibraryKey())
                && request.openLibraryKey().startsWith("/works/");
        String synopsis = isRealOpenLibraryKey
                ? openLibraryClient.fetchWorkDescription(request.openLibraryKey())
                        .map(this::translateSynopsis)
                        .orElse(null)
                : null;

        String fichaTecnica = buildFichaTecnica(request);

        if (StringUtils.hasText(synopsis) && StringUtils.hasText(fichaTecnica)) {
            return synopsis + "\n\n" + fichaTecnica;
        }
        return StringUtils.hasText(synopsis) ? synopsis : fichaTecnica;
    }

    /**
     * A sinopse da Open Library vem em ingles; o produto exige que toda
     * sinopse exibida esteja em portugues. Se a traducao falhar (API fora do
     * ar, cota esgotada), mantemos o texto original em ingles em vez de
     * esconder a sinopse - preferimos mostrar algo real a nada.
     */
    private String translateSynopsis(String englishText) {
        return translationClient.translateToPortuguese(englishText).orElse(englishText);
    }

    /**
     * Traduz sinopses salvas antes da traducao automatica existir (livros
     * adicionados a biblioteca antes desta funcionalidade). Roda no startup
     * (ver DescriptionBackfillRunner) e e idempotente: sinopses que ja
     * parecem estar em portugues sao puladas, entao em execucoes seguintes
     * so os livros realmente pendentes custam uma chamada externa.
     */
    @Transactional
    public int retranslatePendingDescriptions() {
        List<Book> candidates = bookRepository.findByOpenLibraryKeyStartingWithAndDescriptionIsNotNull("/works/");
        int updated = 0;
        for (Book book : candidates) {
            String description = book.getDescription();
            int markerIndex = description.indexOf(FICHA_TECNICA_MARKER);
            String synopsis = markerIndex >= 0 ? description.substring(0, markerIndex) : description;
            String fichaTecnica = markerIndex >= 0 ? description.substring(markerIndex + 2) : null;

            if (looksLikePortuguese(synopsis)) {
                continue;
            }

            String translatedSynopsis = translateSynopsis(synopsis);
            String newDescription =
                    fichaTecnica != null ? translatedSynopsis + "\n\n" + fichaTecnica : translatedSynopsis;
            if (!newDescription.equals(description)) {
                book.updateDescription(newDescription);
                updated++;
            }
        }
        return updated;
    }

    private boolean looksLikePortuguese(String text) {
        String padded = " " + text.toLowerCase() + " ";
        for (String marker : PORTUGUESE_MARKERS) {
            if (padded.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private String buildFichaTecnica(CreateBookRequest request) {
        List<String> facts = new ArrayList<>();
        if (request.pageCount() != null) {
            facts.add(request.pageCount() + " páginas");
        }
        if (StringUtils.hasText(request.genre())) {
            facts.add(request.genre());
        }
        if (request.publishYear() != null) {
            facts.add("publicado em " + request.publishYear());
        }
        if (StringUtils.hasText(request.publisher())) {
            facts.add("pela " + request.publisher());
        }
        return facts.isEmpty() ? null : "Ficha técnica: " + String.join(" · ", facts) + ".";
    }

    private Optional<Book> findExisting(CreateBookRequest request) {
        if (StringUtils.hasText(request.isbn())) {
            Optional<Book> byIsbn = bookRepository.findByIsbn(request.isbn());
            if (byIsbn.isPresent()) {
                return byIsbn;
            }
        }
        if (StringUtils.hasText(request.openLibraryKey())) {
            return bookRepository.findByOpenLibraryKey(request.openLibraryKey());
        }
        return Optional.empty();
    }

    private BookResponseDto toDto(Book book) {
        return new BookResponseDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getCoverUrl(),
                book.getPublishYear(),
                book.getPublisher(),
                book.getPageCount(),
                book.getGenre(),
                book.getDescription(),
                book.getOpenLibraryKey());
    }
}
