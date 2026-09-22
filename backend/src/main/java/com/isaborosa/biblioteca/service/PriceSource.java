package com.isaborosa.biblioteca.service;

import com.isaborosa.biblioteca.domain.book.Book;
import java.util.Optional;

/**
 * Porta para uma fonte externa de preco. Uma fonte que falhar ou nao tiver
 * API oficial disponivel deve ser simplesmente omitida da lista, nunca
 * inventar/estimar um valor.
 */
public interface PriceSource {

    String storeName();

    Optional<PriceQuote> fetchPrice(Book book);
}
