package com.isaborosa.biblioteca.service.recommendation;

import org.springframework.util.StringUtils;

/**
 * Chave usada para deduplicar livros: por ISBN quando existe, senao pela
 * chave da Open Library, senao pelo titulo normalizado.
 */
final class RecommendationSignatures {

    private RecommendationSignatures() {
    }

    static String of(String isbn, String openLibraryKey, String title) {
        if (StringUtils.hasText(isbn)) {
            return "isbn:" + isbn;
        }
        if (StringUtils.hasText(openLibraryKey)) {
            return "key:" + openLibraryKey;
        }
        return "title:" + title.trim().toLowerCase();
    }
}
