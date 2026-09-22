package com.isaborosa.biblioteca.integration.amazon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.isaborosa.biblioteca.domain.book.Book;
import com.isaborosa.biblioteca.integration.amazon.AmazonSignatureV4.SignedRequest;
import com.isaborosa.biblioteca.service.PriceQuote;
import com.isaborosa.biblioteca.service.PriceSource;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * Product Advertising API 5.0 da Amazon (SearchItems). Exige uma conta de
 * Associados <b>aprovada</b> (a Amazon so aprova apos 3 vendas qualificadas
 * em 180 dias) - bem mais restrito que o app gratuito do Mercado Livre. Sem
 * as credenciais configuradas, esta fonte fica desativada (nunca falha com
 * 500 nem inventa preco).
 *
 * <p><b>Atencao:</b> a assinatura AWS SigV4 e o formato de request/response
 * seguem a documentacao oficial da PA-API, mas nao puderam ser testados
 * contra a API real neste ambiente por falta de credenciais aprovadas.
 * Valide com uma chamada real antes de depender disso em producao.
 *
 * @see <a href="https://webservices.amazon.com/paapi5/documentation/">Documentacao da PA-API 5.0</a>
 */
@Component
public class AmazonPriceSource implements PriceSource {

    private static final Logger log = LoggerFactory.getLogger(AmazonPriceSource.class);
    private static final String STORE_NAME = "AMAZON";
    private static final String SERVICE = "ProductAdvertisingAPI";
    private static final String SEARCH_PATH = "/paapi5/searchitems";

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String host;
    private final String region;
    private final String accessKey;
    private final String secretKey;
    private final String partnerTag;
    private final String marketplace;

    public AmazonPriceSource(
            @Value("${app.amazon.host}") String host,
            @Value("${app.amazon.region}") String region,
            @Value("${app.amazon.marketplace}") String marketplace,
            @Value("${app.amazon.timeout-ms}") long timeoutMs,
            @Value("${app.amazon.access-key:}") String accessKey,
            @Value("${app.amazon.secret-key:}") String secretKey,
            @Value("${app.amazon.partner-tag:}") String partnerTag) {
        this.host = host;
        this.region = region;
        this.marketplace = marketplace;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.partnerTag = partnerTag;

        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(timeoutMs))
                .withReadTimeout(Duration.ofMillis(timeoutMs));
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);
        this.restClient = RestClient.builder().baseUrl("https://" + host).requestFactory(requestFactory).build();
    }

    @Override
    public String storeName() {
        return STORE_NAME;
    }

    @Override
    public Optional<PriceQuote> fetchPrice(Book book) {
        if (!isConfigured()) {
            return Optional.empty();
        }

        String keywords = StringUtils.hasText(book.getIsbn())
                ? book.getIsbn()
                : StringUtils.hasText(book.getAuthor()) ? book.getTitle() + " " + book.getAuthor() : book.getTitle();

        try {
            String payload = buildPayload(keywords);
            SignedRequest signed = AmazonSignatureV4.sign(accessKey, secretKey, region, SERVICE, host, SEARCH_PATH, payload);

            String responseBody = restClient.post()
                    .uri(SEARCH_PATH)
                    .headers(headers -> {
                        headers.setContentType(MediaType.valueOf("application/json; charset=UTF-8"));
                        headers.set("Content-Encoding", "amz-1.0");
                        headers.set("X-Amz-Date", signed.amzDate());
                        headers.set("X-Amz-Target", "com.amazon.paapi5.v1.ProductAdvertisingAPIv1.SearchItems");
                        headers.set(HttpHeaders.AUTHORIZATION, signed.authorizationHeader());
                    })
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            return parseCheapestOffer(responseBody);
        } catch (Exception ex) {
            log.warn("Falha ao consultar Amazon para livro id={}: {}", book.getId(), ex.getMessage());
            return Optional.empty();
        }
    }

    private boolean isConfigured() {
        return StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey) && StringUtils.hasText(partnerTag);
    }

    private String buildPayload(String keywords) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("PartnerTag", partnerTag);
        root.put("PartnerType", "Associates");
        root.put("Marketplace", marketplace);
        root.put("Keywords", keywords);
        root.put("SearchIndex", "Books");
        root.put("ItemCount", 3);
        root.putArray("Resources")
                .add("ItemInfo.Title")
                .add("Offers.Listings.Price")
                .add("Images.Primary.Medium");
        return objectMapper.writeValueAsString(root);
    }

    private Optional<PriceQuote> parseCheapestOffer(String responseBody) throws Exception {
        if (responseBody == null) {
            return Optional.empty();
        }
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode items = root.path("SearchResult").path("Items");
        if (!items.isArray() || items.isEmpty()) {
            return Optional.empty();
        }

        BigDecimal bestPrice = null;
        String bestUrl = null;
        for (JsonNode item : items) {
            JsonNode priceNode = item.path("Offers").path("Listings").path(0).path("Price").path("Amount");
            String url = item.path("DetailPageURL").asText(null);
            if (priceNode.isMissingNode() || !StringUtils.hasText(url)) {
                continue;
            }
            BigDecimal price = new BigDecimal(priceNode.asText());
            if (bestPrice == null || price.compareTo(bestPrice) < 0) {
                bestPrice = price;
                bestUrl = url;
            }
        }
        return bestPrice != null ? Optional.of(new PriceQuote(bestPrice, bestUrl)) : Optional.empty();
    }
}
