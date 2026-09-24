package com.isaborosa.biblioteca.config;

import com.isaborosa.biblioteca.dto.ApiErrorResponse;
import com.isaborosa.biblioteca.exception.BookNotFoundException;
import com.isaborosa.biblioteca.exception.DuplicateBookInLibraryException;
import com.isaborosa.biblioteca.exception.InvalidCredentialsException;
import com.isaborosa.biblioteca.exception.InvalidCurrentPageException;
import com.isaborosa.biblioteca.exception.RatingRequiredException;
import com.isaborosa.biblioteca.exception.UserBookNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fieldError -> fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("VALIDATION_ERROR", "Dados de entrada invalidos", fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("MALFORMED_REQUEST_BODY", "Corpo da requisicao invalido ou mal formado"));
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleBookNotFound(BookNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("BOOK_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(UserBookNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserBookNotFound(UserBookNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("USER_BOOK_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateBookInLibraryException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(DuplicateBookInLibraryException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("DUPLICATE_BOOK_IN_LIBRARY", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse("INVALID_CREDENTIALS", ex.getMessage()));
    }

    @ExceptionHandler(RatingRequiredException.class)
    public ResponseEntity<ApiErrorResponse> handleRatingRequired(RatingRequiredException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("RATING_REQUIRED", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCurrentPageException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCurrentPage(InvalidCurrentPageException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("INVALID_CURRENT_PAGE", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("DATA_INTEGRITY_VIOLATION", "Operacao viola uma restricao do banco de dados"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        log.error("Erro inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse("INTERNAL_ERROR", "Ocorreu um erro inesperado"));
    }
}
