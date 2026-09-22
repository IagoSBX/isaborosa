package com.isaborosa.biblioteca.integration.amazon;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Assinatura AWS Signature Version 4 para a Product Advertising API 5.0 da
 * Amazon. Implementacao segue exatamente o algoritmo documentado pela AWS
 * (canonical request -> string to sign -> signing key -> Authorization
 * header). Nao foi possivel testar contra a API real neste ambiente por
 * falta de credenciais de uma conta de Associados aprovada - revise com uma
 * chamada real antes de depender disso em producao.
 *
 * @see <a href="https://docs.aws.amazon.com/general/latest/gr/sigv4-signed-request-examples.html">AWS SigV4</a>
 */
final class AmazonSignatureV4 {

    private static final DateTimeFormatter AMZ_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter DATE_STAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    private AmazonSignatureV4() {
    }

    record SignedRequest(String amzDate, String authorizationHeader) {
    }

    static SignedRequest sign(
            String accessKey, String secretKey, String region, String service,
            String host, String path, String payload) {
        try {
            java.time.Instant now = java.time.Instant.now().truncatedTo(ChronoUnit.SECONDS);
            String amzDate = AMZ_DATE_FORMAT.format(now);
            String dateStamp = DATE_STAMP_FORMAT.format(now);

            String signedHeaders = "content-encoding;content-type;host;x-amz-date;x-amz-target";
            String canonicalHeaders = "content-encoding:amz-1.0\n"
                    + "content-type:application/json; charset=UTF-8\n"
                    + "host:" + host + "\n"
                    + "x-amz-date:" + amzDate + "\n"
                    + "x-amz-target:com.amazon.paapi5.v1.ProductAdvertisingAPIv1.SearchItems\n";

            String payloadHash = sha256Hex(payload);
            String canonicalRequest = "POST\n" + path + "\n\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + payloadHash;

            String credentialScope = dateStamp + "/" + region + "/" + service + "/aws4_request";
            String stringToSign = "AWS4-HMAC-SHA256\n" + amzDate + "\n" + credentialScope + "\n"
                    + sha256Hex(canonicalRequest);

            byte[] kDate = hmacSha256(("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8), dateStamp);
            byte[] kRegion = hmacSha256(kDate, region);
            byte[] kService = hmacSha256(kRegion, service);
            byte[] kSigning = hmacSha256(kService, "aws4_request");
            String signature = hex(hmacSha256(kSigning, stringToSign));

            String authorizationHeader = "AWS4-HMAC-SHA256 Credential=" + accessKey + "/" + credentialScope
                    + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;

            return new SignedRequest(amzDate, authorizationHeader);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao assinar requisicao para a Amazon PA-API", ex);
        }
    }

    private static byte[] hmacSha256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256Hex(String data) throws NoSuchAlgorithmException {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(data.getBytes(StandardCharsets.UTF_8));
        return hex(hash);
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
