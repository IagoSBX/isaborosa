package com.isaborosa.biblioteca.integration.googlebooks;

import com.isaborosa.biblioteca.dto.BookSearchResultDto;
import com.isaborosa.biblioteca.integration.openlibrary.GenreTranslations;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * Segunda fonte de busca de livros, para termos que a Open Library nao cobre
 * bem. A API do Google Books hoje exige uma API key mesmo para uso anonimo
 * (o antigo acesso sem chave passou a retornar 429 "quota exceeded"). Sem
 * {@code app.googlebooks.api-key} configurada, esta fonte fica desativada e
 * devolve lista vazia - a busca continua funcionando so com a Open Library.
 * Uma API key e gratuita e instantanea via Google Cloud Console (ative
 * "Books API" e gere uma chave), bem mais simples que Amazon/Mercado Livre.
 */
@Component
public class GoogleBooksClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksClient.class);

    private final RestClient restClient;
    private final String apiKey;

    public GoogleBooksClient(
            @Value("${app.googlebooks.base-url}") String baseUrl,
            @Value("${app.googlebooks.timeout-ms}") long timeoutMs,
            @Value("${app.googlebooks.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(timeoutMs))
                .withReadTimeout(Duration.ofMillis(timeoutMs));
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    public List<BookSearchResultDto> search(String query, int startIndex, int maxResults) {
        if (!isConfigured()) {
            return List.of();
        }
        try {
            GoogleBooksResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/books/v1/volumes")
                            .queryParam("q", query)
                            .queryParam("startIndex", startIndex)
                            .queryParam("maxResults", maxResults)
                            .queryParam("printType", "books")
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(GoogleBooksResponse.class);
            if (response == null || response.items() == null) {
                return List.of();
            }
            return response.items().stream()
                    .map(this::toDto)
                    .filter(dto -> StringUtils.hasText(dto.title()))
                    .toList();
        } catch (Exception ex) {
            log.warn("Falha ao consultar Google Books para query='{}': {}", query, ex.getMessage());
            return List.of();
        }
    }

    private BookSearchResultDto toDto(GoogleBooksResponse.Item item) {
        GoogleBooksResponse.VolumeInfo info = item.volumeInfo();
        if (info == null) {
            return new BookSearchResultDto(null, null, null, null, null, null, null, null, null);
        }
        String author = info.authors() == null || info.authors().isEmpty() ? null : String.join(", ", info.authors());
        String isbn = extractIsbn(info);
        String coverUrl = info.imageLinks() != null
                ? (StringUtils.hasText(info.imageLinks().thumbnail()) ? info.imageLinks().thumbnail() : info.imageLinks().smallThumbnail())
                : null;
        Integer publishYear = extractYear(info.publishedDate());
        String genre = extractGenre(info.categories());

        return new BookSearchResultDto(
                info.title(),
                author,
                isbn,
                coverUrl,
                publishYear,
                info.publisher(),
                info.pageCount(),
                genre,
                "googlebooks:" + item.id());
    }

    private String extractIsbn(GoogleBooksResponse.VolumeInfo info) {
        if (info.industryIdentifiers() == null) {
            return null;
        }
        return info.industryIdentifiers().stream()
                .filter(id -> "ISBN_13".equals(id.type()))
                .map(GoogleBooksResponse.IndustryIdentifier::identifier)
                .findFirst()
                .orElseGet(() -> info.industryIdentifiers().stream()
                        .filter(id -> "ISBN_10".equals(id.type()))
                        .map(GoogleBooksResponse.IndustryIdentifier::identifier)
                        .findFirst()
                        .orElse(null));
    }

    private Integer extractYear(String publishedDate) {
        if (!StringUtils.hasText(publishedDate) || publishedDate.length() < 4) {
            return null;
        }
        try {
            return Integer.parseInt(publishedDate.substring(0, 4));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String extractGenre(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }
        for (String category : categories) {
            for (String part : category.split("\\s*/\\s*")) {
                String translated = GenreTranslations.toPortuguese(part);
                if (translated != null) {
                    return translated;
                }
            }
        }
        return null;
    }
}
