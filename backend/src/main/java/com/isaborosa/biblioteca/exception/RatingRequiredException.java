package com.isaborosa.biblioteca.exception;

public class RatingRequiredException extends RuntimeException {

    public RatingRequiredException() {
        super("E preciso avaliar de 1 a 5 estrelas ao marcar um livro como 'Ja li'");
    }
}
