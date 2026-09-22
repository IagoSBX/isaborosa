package com.isaborosa.biblioteca.exception;

public class DuplicateBookInLibraryException extends RuntimeException {

    public DuplicateBookInLibraryException(Long bookId) {
        super("Livro ja esta na biblioteca: bookId=" + bookId);
    }
}
