package by.oma.academy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Запрос на регистрацию нового пользователя. */
public record RegisterRequest(
        @NotBlank(message = "Введите имя")
        @Size(max = 100, message = "Имя слишком длинное")
        String name,

        @NotBlank(message = "Введите email")
        @Email(message = "Некорректный email")
        @Size(max = 254)
        String email,

        @NotBlank
        @Size(min = 8, max = 72, message = "Пароль: от 8 до 72 символов")
        String password,

        String audience
) {}
