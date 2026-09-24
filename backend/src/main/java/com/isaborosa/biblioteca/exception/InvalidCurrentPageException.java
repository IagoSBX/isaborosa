package com.isaborosa.biblioteca.exception;

public class InvalidCurrentPageException extends RuntimeException {

    public InvalidCurrentPageException(int currentPage, int pageCount) {
        super("Pagina atual (" + currentPage + ") nao pode ser maior que o total de paginas do livro (" + pageCount
                + ")");
    }
}
