package com.isaborosa.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8, max = 72, message = "a nova senha precisa ter pelo menos 8 caracteres") String newPassword) {
}
