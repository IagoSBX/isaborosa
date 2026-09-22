package com.isaborosa.biblioteca.exception;

public class UserBookNotFoundException extends RuntimeException {

    public UserBookNotFoundException(Long id) {
        super("Item da biblioteca nao encontrado: id=" + id);
    }
}
