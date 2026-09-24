package com.isaborosa.biblioteca.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Traduz, no startup, sinopses de livros salvos antes da traducao automatica
 * existir. Idempotente (ver BookService.retranslatePendingDescriptions) -
 * livros ja traduzidos nao custam nenhuma chamada externa nas proximas vezes.
 */
@Component
class DescriptionBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DescriptionBackfillRunner.class);

    private final BookService bookService;

    DescriptionBackfillRunner(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void run(ApplicationArguments args) {
        int updated = bookService.retranslatePendingDescriptions();
        if (updated > 0) {
            log.info("Sinopses traduzidas para portugues no startup: {}", updated);
        }
    }
}
