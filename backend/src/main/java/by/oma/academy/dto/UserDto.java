package by.oma.academy.dto;

/** DTO пользователя — безопасен для отдачи клиенту (без хеша пароля). */
public record UserDto(Long id, String name, String email, String audience) {}
