package com.isaborosa.biblioteca.integration.mercadolivre;

import com.isaborosa.biblioteca.domain.book.Book;
import com.isaborosa.biblioteca.service.PriceQuote;
import com.isaborosa.biblioteca.service.PriceSource;
import java.time.Duration;
import java.util.Comparator;
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
 * API oficial do Mercado Livre (sem scraping). Busca por ISBN quando
 * disponivel, que e mais preciso que buscar por titulo.
 *
 * <p>O endpoint de busca por site (/sites/MLB/search) atualmente exige um
 * access token OAuth para requisicoes anonimas (retorna 403 sem ele) - isso
 * mudou desde que a API era totalmente publica. O token e resolvido por
 * {@link MercadoLivreTokenProvider} (token estatico ou client_credentials);
 * sem nenhum configurado, esta fonte e omitida da lista de precos (nunca
 * falha com 500 nem inventa valor).
 */
@Component
public class MercadoLivrePriceSource implements PriceSource {

    private static final Logger log = LoggerFactory.getLogger(MercadoLivrePriceSource.class);
    private static final String STORE_NAME = "MERCADO_LIVRE";

    private final RestClient restClient;
    private final MercadoLivreTokenProvider tokenProvider;

    public MercadoLivrePriceSource(
            @Value("${app.mercadolivre.base-url}") String baseUrl,
            @Value("${app.mercadolivre.timeout-ms}") long timeoutMs,
            MercadoLivreTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(timeoutMs))
                .withReadTimeout(Duration.ofMillis(timeoutMs));
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @Override
    public String storeName() {
        return STORE_NAME;
    }

    @Override
    public Optional<PriceQuote> fetchPrice(Book book) {
        String query = StringUtils.hasText(book.getIsbn()) ? book.getIsbn() : queryFromTitleAndAuthor(book);
        try {
            MercadoLivreSearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/sites/MLB/search")
                            .queryParam("q", query)
                            .queryParam("limit", 5)
                            .build())
                    .headers(headers -> tokenProvider.getAccessToken().ifPresent(headers::setBearerAuth))
                    .retrieve()
                    .body(MercadoLivreSearchResponse.class);

            if (response == null || response.results() == null) {
                return Optional.empty();
            }
            return response.results().stream()
                    .filter(result -> result.price() != null && StringUtils.hasText(result.permalink()))
                    .min(Comparator.comparing(MercadoLivreSearchResponse.Result::price))
                    .map(result -> new PriceQuote(result.price(), result.permalink()));
        } catch (Exception ex) {
            log.warn("Falha ao consultar Mercado Livre para livro id={}: {}", book.getId(), ex.getMessage());
            return Optional.empty();
        }
    }

    private String queryFromTitleAndAuthor(Book book) {
        return StringUtils.hasText(book.getAuthor())
                ? book.getTitle() + " " + book.getAuthor()
                : book.getTitle();
    }
}
