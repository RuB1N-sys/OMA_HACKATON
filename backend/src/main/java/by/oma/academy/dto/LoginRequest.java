package by.oma.academy.dto;

import jakarta.validation.constraints.NotBlank;

/** Запрос на вход в систему. */
public record LoginRequest(
        @NotBlank(message = "Введите email")
        String email,

        @NotBlank(message = "Введите пароль")
        String password
) {}
