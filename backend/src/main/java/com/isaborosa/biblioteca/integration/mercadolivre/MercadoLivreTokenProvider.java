package com.isaborosa.biblioteca.integration.mercadolivre;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * Resolve o token de acesso para a API do Mercado Livre, na ordem:
 * 1) token estatico (MERCADOLIVRE_ACCESS_TOKEN), se configurado;
 * 2) client_credentials (MERCADOLIVRE_CLIENT_ID/SECRET), trocado e cacheado
 *    em memoria ate perto de expirar;
 * 3) nenhum (requisicoes anonimas - hoje retornam 403 no endpoint de busca).
 *
 * <p>Um app "sem necessidade de autorizacao do usuario" pode ser criado
 * gratuitamente em https://developers.mercadolivre.com.br/ para obter
 * client id/secret (nao exige login do usuario final, so do dono do app).
 */
@Component
class MercadoLivreTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(MercadoLivreTokenProvider.class);
    private static final Duration EXPIRY_SAFETY_MARGIN = Duration.ofSeconds(30);

    private final RestClient restClient;
    private final String staticAccessToken;
    private final String clientId;
    private final String clientSecret;

    private volatile String cachedToken;
    private volatile Instant cachedTokenExpiresAt = Instant.MIN;

    MercadoLivreTokenProvider(
            RestClient.Builder restClientBuilder,
            @Value("${app.mercadolivre.base-url}") String baseUrl,
            @Value("${app.mercadolivre.access-token:}") String staticAccessToken,
            @Value("${app.mercadolivre.client-id:}") String clientId,
            @Value("${app.mercadolivre.client-secret:}") String clientSecret) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.staticAccessToken = staticAccessToken;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    synchronized Optional<String> getAccessToken() {
        if (StringUtils.hasText(staticAccessToken)) {
            return Optional.of(staticAccessToken);
        }

        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)) {
            return Optional.empty();
        }

        if (cachedToken != null && Instant.now().isBefore(cachedTokenExpiresAt)) {
            return Optional.of(cachedToken);
        }

        return requestNewToken();
    }

    private Optional<String> requestNewToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        try {
            MercadoLivreTokenResponse response = restClient.post()
                    .uri("/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(MercadoLivreTokenResponse.class);

            if (response == null || !StringUtils.hasText(response.access_token())) {
                return Optional.empty();
            }

            cachedToken = response.access_token();
            long expiresInSeconds = response.expires_in() != null ? response.expires_in() : 21600;
            cachedTokenExpiresAt = Instant.now().plusSeconds(expiresInSeconds).minus(EXPIRY_SAFETY_MARGIN);
            return Optional.of(cachedToken);
        } catch (Exception ex) {
            log.warn("Falha ao obter token OAuth do Mercado Livre: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
