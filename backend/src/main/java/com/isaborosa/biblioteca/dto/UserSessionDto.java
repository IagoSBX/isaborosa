package com.isaborosa.biblioteca.dto;

/** Nunca inclui password_hash - so o que o frontend precisa saber sobre a sessao. */
public record UserSessionDto(String username, String name, boolean mustChangePassword) {
}
