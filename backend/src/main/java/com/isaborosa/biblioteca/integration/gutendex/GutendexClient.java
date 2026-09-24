package com.isaborosa.biblioteca.integration.gutendex;

import com.isaborosa.biblioteca.dto.FreeSourceDto;
import java.text.Normalizer;
import java.time.Duration;
import java.util.Optional;
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
 * Cliente para o Gutendex (wrapper de metadados do Project Gutenberg, sem
 * chave de API). So expoe um livro como fonte gratuita quando o resultado
 * bate com o titulo (e autor, se disponivel) pedidos - nunca "o primeiro
 * resultado que aparecer", para nao sugerir o livro errado como gratuito.
 */
@Component
public class GutendexClient {

    private static final Logger log = LoggerFactory.getLogger(GutendexClient.class);

    /**
     * Ordem de preferencia de formato para "ler agora": HTML no navegador,
     * depois EPUB, depois texto puro. Chaves do Gutendex variam com sufixo de
     * charset, por isso o match e por prefixo.
     */
    private static final String[] FORMAT_PREFERENCE = {"text/html", "application/epub+zip", "text/plain"};

    private final RestClient restClient;

    public GutendexClient(
            @Value("${app.gutendex.base-url}") String baseUrl,
            @Value("${app.gutendex.timeout-ms}") long timeoutMs) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(timeoutMs))
                .withReadTimeout(Duration.ofMillis(timeoutMs));
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    public Optional<FreeSourceDto> findFreeBook(String title, String author) {
        if (!StringUtils.hasText(title)) {
            return Optional.empty();
        }
        try {
            String query = StringUtils.hasText(author) ? title + " " + author : title;
            GutendexResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/books/").queryParam("search", query).build())
                    .retrieve()
                    .body(GutendexResponse.class);
            if (response == null || response.results() == null) {
                return Optional.empty();
            }
            return response.results().stream()
                    .filter(result -> matches(result, title, author))
                    .findFirst()
                    .flatMap(this::toFreeSource);
        } catch (Exception ex) {
            log.warn("Falha ao consultar Gutendex para '{}': {}", title, ex.getMessage());
            return Optional.empty();
        }
    }

    private boolean matches(GutendexResponse.Result result, String title, String author) {
        if (!StringUtils.hasText(result.title())) {
            return false;
        }
        String normalizedResultTitle = normalize(result.title());
        String normalizedRequestedTitle = normalize(title);
        boolean titleMatches = normalizedResultTitle.contains(normalizedRequestedTitle)
                || normalizedRequestedTitle.contains(normalizedResultTitle);
        if (!titleMatches) {
            return false;
        }
        if (!StringUtils.hasText(author) || result.authors() == null || result.authors().isEmpty()) {
            return titleMatches;
        }
        String normalizedRequestedAuthor = normalize(author);
        return result.authors().stream()
                .filter(a -> StringUtils.hasText(a.name()))
                .anyMatch(a -> {
                    String normalizedAuthorName = normalize(a.name());
                    return normalizedRequestedAuthor.contains(normalizedAuthorName)
                            || normalizedAuthorName.contains(normalizedRequestedAuthor)
                            || sharesAWord(normalizedRequestedAuthor, normalizedAuthorName);
                });
    }

    private boolean sharesAWord(String a, String b) {
        for (String word : a.split("[\\s,]+")) {
            if (word.length() > 2 && b.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String text) {
        String withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase().replaceAll("[^a-z0-9\\s,]", "").trim();
    }

    private Optional<FreeSourceDto> toFreeSource(GutendexResponse.Result result) {
        if (result.formats() == null) {
            return Optional.empty();
        }
        for (String preferredFormat : FORMAT_PREFERENCE) {
            for (var entry : result.formats().entrySet()) {
                if (entry.getKey().startsWith(preferredFormat) && StringUtils.hasText(entry.getValue())) {
                    return Optional.of(new FreeSourceDto("PROJECT_GUTENBERG", "Project Gutenberg", entry.getValue()));
                }
            }
        }
        return Optional.empty();
    }
}
