package com.isaborosa.biblioteca.integration.openlibrary;

import com.fasterxml.jackson.databind.JsonNode;
import com.isaborosa.biblioteca.dto.BookSearchResultDto;
import com.isaborosa.biblioteca.dto.FreeSourceDto;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente dedicado a Open Library. Falha de terceiro nunca deve virar 500 para
 * o cliente da nossa API: em caso de erro ou timeout, devolve lista vazia e loga.
 */
@Component
public class OpenLibraryClient {

    private static final Logger log = LoggerFactory.getLogger(OpenLibraryClient.class);

    private static final String DEFAULT_SEARCH_FIELDS =
            "title,author_name,isbn,cover_i,first_publish_year,publisher,number_of_pages_median,subject,key";

    private static final String AVAILABILITY_FIELDS = "ia,ebook_access";

    /**
     * Identificadores de "ia" que nao levam a uma leitura de fato: "bwb_*" sao
     * apenas registros de disponibilidade fisica (Better World Books), e
     * "_librivox" e audiolivro, nao texto. Filtramos os dois para nunca
     * oferecer um link que nao abre o livro para leitura gratuita de verdade.
     */
    private static final String[] UNUSABLE_IA_PATTERNS = {"bwb_", "librivox"};

    private final RestClient restClient;
    private final String coversBaseUrl;

    public OpenLibraryClient(
            @Value("${app.openlibrary.base-url}") String baseUrl,
            @Value("${app.openlibrary.covers-base-url}") String coversBaseUrl,
            @Value("${app.openlibrary.timeout-ms}") long timeoutMs) {
        this.coversBaseUrl = coversBaseUrl;
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(timeoutMs))
                .withReadTimeout(Duration.ofMillis(timeoutMs));
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public List<BookSearchResultDto> search(String query, int offset, int limit) {
        return searchByParam("q", query, offset, limit);
    }

    public List<BookSearchResultDto> searchByAuthor(String author, int limit) {
        return searchByParam("author", author, 0, limit);
    }

    public List<BookSearchResultDto> searchByGenre(String portugueseGenre, int offset, int limit) {
        String englishSubject = GenreTranslations.toEnglish(portugueseGenre);
        if (englishSubject == null) {
            return List.of();
        }
        return searchByParam("subject", englishSubject, offset, limit);
    }

    /**
     * Livros realmente em alta na Open Library (baseado em acessos recentes,
     * nao um numero inventado) - usado na secao "Livros populares".
     */
    public List<BookSearchResultDto> fetchTrending(int limit) {
        try {
            OpenLibraryTrendingResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/trending/daily.json")
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .body(OpenLibraryTrendingResponse.class);
            if (response == null || response.works() == null) {
                return List.of();
            }
            return response.works().stream().map(this::toDto).toList();
        } catch (Exception ex) {
            log.warn("Falha ao consultar livros em alta na Open Library: {}", ex.getMessage());
            return List.of();
        }
    }

    private List<BookSearchResultDto> searchByParam(String paramName, String paramValue, int offset, int limit) {
        try {
            OpenLibrarySearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search.json")
                            .queryParam(paramName, paramValue)
                            .queryParam("offset", offset)
                            .queryParam("limit", limit)
                            .queryParam("fields", DEFAULT_SEARCH_FIELDS)
                            .build())
                    .retrieve()
                    .body(OpenLibrarySearchResponse.class);
            if (response == null || response.docs() == null) {
                return List.of();
            }
            return response.docs().stream().map(this::toDto).toList();
        } catch (Exception ex) {
            log.warn("Falha ao consultar Open Library para {}='{}': {}", paramName, paramValue, ex.getMessage());
            return List.of();
        }
    }

    private BookSearchResultDto toDto(OpenLibrarySearchResponse.Doc doc) {
        String author = doc.author_name() == null || doc.author_name().isEmpty()
                ? null
                : String.join(", ", doc.author_name());
        String isbn = doc.isbn() == null || doc.isbn().isEmpty() ? null : doc.isbn().get(0);
        String publisher = doc.publisher() == null || doc.publisher().isEmpty() ? null : doc.publisher().get(0);
        String coverUrl = doc.cover_i() == null ? null : coversBaseUrl + "/b/id/" + doc.cover_i() + "-L.jpg";
        String genre = primaryGenre(doc.subject());
        return new BookSearchResultDto(
                doc.title(),
                author,
                isbn,
                coverUrl,
                doc.first_publish_year(),
                publisher,
                doc.number_of_pages_median(),
                genre,
                doc.key());
    }

    private BookSearchResultDto toDto(OpenLibraryTrendingResponse.Work work) {
        String author = work.author_name() == null || work.author_name().isEmpty()
                ? null
                : String.join(", ", work.author_name());
        String coverUrl = work.cover_i() == null ? null : coversBaseUrl + "/b/id/" + work.cover_i() + "-L.jpg";
        return new BookSearchResultDto(
                work.title(), author, null, coverUrl, work.first_publish_year(), null, null, null, work.key());
    }

    private String primaryGenre(List<String> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            return null;
        }
        return subjects.stream()
                .map(GenreTranslations::toPortuguese)
                .filter(translated -> translated != null)
                .findFirst()
                .orElse(null);
    }

    /**
     * Busca a sinopse real de um livro pela sua "work key" da Open Library
     * (ex.: "/works/OL27482W"). O campo "description" desse endpoint vem ora
     * como string simples, ora como objeto {"type":..., "value": "..."} -
     * tratamos os dois formatos. Falha ou ausencia de sinopse retorna vazio,
     * nunca lanca excecao (o chamador decide o que fazer sem sinopse).
     */
    public Optional<String> fetchWorkDescription(String openLibraryKey) {
        try {
            JsonNode work = restClient.get()
                    .uri(openLibraryKey + ".json")
                    .retrieve()
                    .body(JsonNode.class);
            if (work == null) {
                return Optional.empty();
            }
            JsonNode description = work.get("description");
            if (description == null) {
                return Optional.empty();
            }
            String text = description.isTextual() ? description.asText() : description.path("value").asText(null);
            if (text == null || text.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(cleanDescription(text));
        } catch (Exception ex) {
            log.warn("Falha ao buscar sinopse para '{}': {}", openLibraryKey, ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Sinopses da Open Library as vezes trazem um separador "---" seguido de
     * metadados (links para volumes da serie, notas de edicao) e links em
     * formato markdown. Mantemos so o texto corrido da sinopse.
     */
    private String cleanDescription(String rawText) {
        String withoutMetadataSection = rawText.split("\r?\n-{3,}")[0];
        String withoutMarkdownLinks = withoutMetadataSection.replaceAll("\\[([^]]+)]\\([^)]+\\)", "$1");
        return withoutMarkdownLinks.trim();
    }

    /**
     * Verifica se o livro tem uma copia de leitura publica e gratuita no
     * Internet Archive. So retorna algo quando "ebook_access" e exatamente
     * "public" (acesso imediato, sem "emprestimo") e existe pelo menos um
     * identificador de "ia" utilizavel de fato (ver UNUSABLE_IA_PATTERNS).
     * Nunca inventa um link: ausencia de dado real vira Optional.empty().
     */
    public Optional<FreeSourceDto> fetchInternetArchiveSource(String isbn, String title, String author) {
        String query = StringUtils.hasText(isbn)
                ? "isbn:" + isbn
                : buildTitleAuthorQuery(title, author);
        if (!StringUtils.hasText(query)) {
            return Optional.empty();
        }
        try {
            OpenLibrarySearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search.json")
                            .queryParam("q", query)
                            .queryParam("limit", 1)
                            .queryParam("fields", AVAILABILITY_FIELDS)
                            .build())
                    .retrieve()
                    .body(OpenLibrarySearchResponse.class);
            if (response == null || response.docs() == null || response.docs().isEmpty()) {
                return Optional.empty();
            }
            OpenLibrarySearchResponse.Doc doc = response.docs().get(0);
            if (!"public".equals(doc.ebook_access()) || doc.ia() == null) {
                return Optional.empty();
            }
            return doc.ia().stream()
                    .filter(this::isUsableIaId)
                    .findFirst()
                    .map(iaId -> new FreeSourceDto(
                            "INTERNET_ARCHIVE", "Internet Archive", "https://archive.org/details/" + iaId));
        } catch (Exception ex) {
            log.warn("Falha ao consultar disponibilidade gratuita no Internet Archive: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private boolean isUsableIaId(String iaId) {
        String lower = iaId.toLowerCase();
        for (String pattern : UNUSABLE_IA_PATTERNS) {
            if (lower.contains(pattern)) {
                return false;
            }
        }
        return true;
    }

    private String buildTitleAuthorQuery(String title, String author) {
        if (!StringUtils.hasText(title)) {
            return null;
        }
        return StringUtils.hasText(author) ? title + " " + author : title;
    }
}
