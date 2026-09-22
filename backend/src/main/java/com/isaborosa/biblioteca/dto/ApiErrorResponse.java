package com.isaborosa.biblioteca.dto;

import java.util.Map;

public record ApiErrorResponse(String error, String message, Map<String, String> fieldErrors) {

    public ApiErrorResponse(String error, String message) {
        this(error, message, null);
    }
}
