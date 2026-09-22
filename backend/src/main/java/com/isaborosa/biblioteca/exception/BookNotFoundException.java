package com.isaborosa.biblioteca.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("Livro nao encontrado: id=" + id);
    }
}
