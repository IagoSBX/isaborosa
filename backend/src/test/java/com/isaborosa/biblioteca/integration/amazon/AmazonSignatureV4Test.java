package com.isaborosa.biblioteca.integration.amazon;

import static org.assertj.core.api.Assertions.assertThat;

import com.isaborosa.biblioteca.integration.amazon.AmazonSignatureV4.SignedRequest;
import org.junit.jupiter.api.Test;

/**
 * Nao ha vetores de teste oficiais publicados para a PA-API, entao este
 * teste verifica a forma do resultado (estrutura do header, tamanho da
 * assinatura, determinismo dos componentes fixos), nao a corretude fim a fim
 * contra a Amazon real - isso so pode ser validado com uma chamada de
 * verdade usando credenciais aprovadas.
 */
class AmazonSignatureV4Test {

    @Test
    void geraAuthorizationHeaderComEstruturaEsperada() {
        SignedRequest signed = AmazonSignatureV4.sign(
                "AKIDEXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
                "us-east-1", "ProductAdvertisingAPI",
                "webservices.amazon.com.br", "/paapi5/searchitems",
                "{\"Keywords\":\"test\"}");

        assertThat(signed.amzDate()).matches("\\d{8}T\\d{6}Z");
        assertThat(signed.authorizationHeader())
                .startsWith("AWS4-HMAC-SHA256 Credential=AKIDEXAMPLE/")
                .contains("/us-east-1/ProductAdvertisingAPI/aws4_request")
                .contains("SignedHeaders=content-encoding;content-type;host;x-amz-date;x-amz-target")
                .containsPattern("Signature=[0-9a-f]{64}$");
    }

    @Test
    void mesmaEntradaProduzMesmaAssinaturaDentroDoMesmoSegundo() {
        SignedRequest first = AmazonSignatureV4.sign(
                "AKID", "secret", "us-east-1", "ProductAdvertisingAPI",
                "webservices.amazon.com.br", "/paapi5/searchitems", "{}");
        SignedRequest second = AmazonSignatureV4.sign(
                "AKID", "secret", "us-east-1", "ProductAdvertisingAPI",
                "webservices.amazon.com.br", "/paapi5/searchitems", "{}");

        // mesmo payload e mesmo instante (truncado ao segundo) devem assinar igual
        if (first.amzDate().equals(second.amzDate())) {
            assertThat(first.authorizationHeader()).isEqualTo(second.authorizationHeader());
        }
    }
}
