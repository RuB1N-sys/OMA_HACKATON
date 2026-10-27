package by.oma.academy.dto;

/** Ответ с JWT-токеном и данными пользователя. */
public record AuthResponse(String token, UserDto user) {}
