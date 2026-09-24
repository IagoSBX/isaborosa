package com.isaborosa.biblioteca.dto;

/**
 * Uma fonte legal e gratuita para ler o livro (nunca pirataria). "store" e um
 * identificador estavel (ex.: "PROJECT_GUTENBERG"), "label" o nome exibido e
 * "url" o link direto para leitura.
 */
public record FreeSourceDto(String store, String label, String url) {
}
