package com.isaborosa.biblioteca.integration.translation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
 * Traduz sinopses (sempre em ingles, vindas da Open Library) para portugues
 * usando a API gratuita MyMemory. Regra de produto: toda sinopse exibida deve
 * estar em portugues; se a traducao falhar, o chamador decide o fallback
 * (nunca lancamos excecao daqui). O texto original serve de chave de cache em
 * memoria para nao traduzir o mesmo livro de novo a cada consulta.
 *
 * Limite conhecido: a API gratuita do MyMemory tem cota diaria por IP
 * (~5000 palavras sem e-mail, ~50000 com "app.translation.email" preenchido)
 * e um limite de ~500 caracteres por requisicao - por isso o texto e
 * dividido em pedacos por frase antes de traduzir.
 */
@Component
public class TranslationClient {

    private static final Logger log = LoggerFactory.getLogger(TranslationClient.class);
    private static final int MAX_CHUNK_LENGTH = 450;

    private final RestClient restClient;
    private final String contactEmail;
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public TranslationClient(
            @Value("${app.translation.base-url}") String baseUrl,
            @Value("${app.translation.timeout-ms}") long timeoutMs,
            @Value("${app.translation.email:}") String contactEmail) {
        this.contactEmail = contactEmail;
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(timeoutMs))
                .withReadTimeout(Duration.ofMillis(timeoutMs));
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    public Optional<String> translateToPortuguese(String text) {
        if (!StringUtils.hasText(text)) {
            return Optional.empty();
        }
        String cached = cache.get(text);
        if (cached != null) {
            return Optional.of(cached);
        }
        try {
            String translated = splitIntoChunks(text).stream()
                    .map(this::translateChunk)
                    .reduce((a, b) -> a + " " + b)
                    .orElse(null);
            if (!StringUtils.hasText(translated)) {
                return Optional.empty();
            }
            cache.put(text, translated);
            return Optional.of(translated);
        } catch (Exception ex) {
            log.warn("Falha ao traduzir sinopse para portugues: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private String translateChunk(String chunk) {
        MyMemoryResponse response = restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/get").queryParam("q", chunk).queryParam("langpair", "en|pt-BR");
                    if (StringUtils.hasText(contactEmail)) {
                        uriBuilder.queryParam("de", contactEmail);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .body(MyMemoryResponse.class);
        if (response == null || response.responseData() == null
                || !StringUtils.hasText(response.responseData().translatedText())) {
            throw new IllegalStateException("resposta vazia da API de traducao");
        }
        return response.responseData().translatedText();
    }

    /**
     * Divide em frases (limite de tamanho por requisicao da API gratuita);
     * uma frase isolada maior que o limite e quebrada em pedacos fixos como
     * ultimo recurso.
     */
    private List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String sentence : text.split("(?<=[.!?])\\s+")) {
            if (sentence.length() > MAX_CHUNK_LENGTH) {
                flushIfNotEmpty(chunks, current);
                for (int i = 0; i < sentence.length(); i += MAX_CHUNK_LENGTH) {
                    chunks.add(sentence.substring(i, Math.min(i + MAX_CHUNK_LENGTH, sentence.length())));
                }
                continue;
            }
            if (current.length() + sentence.length() + 1 > MAX_CHUNK_LENGTH) {
                flushIfNotEmpty(chunks, current);
            }
            if (current.length() > 0) {
                current.append(' ');
            }
            current.append(sentence);
        }
        flushIfNotEmpty(chunks, current);
        return chunks;
    }

    private void flushIfNotEmpty(List<String> chunks, StringBuilder current) {
        if (current.length() > 0) {
            chunks.add(current.toString());
            current.setLength(0);
        }
    }
}
