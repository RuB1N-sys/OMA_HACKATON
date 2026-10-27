package by.oma.academy.service;

import by.oma.academy.exception.UnauthorizedException;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthHelper {

    private final JwtService jwtService;

    public AuthHelper(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Извлекает userId из заголовка Authorization.
     * Выбрасывает UnauthorizedException при отсутствии или невалидности токена.
     */
    public Long requireUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Требуется авторизация");
        }
        try {
            return jwtService.extractUserId(authHeader.substring(7));
        } catch (JwtException | NumberFormatException e) {
            throw new UnauthorizedException("Недействительный или истекший токен");
        }
    }

    /**
     * Извлекает userId, если передан валидный токен; иначе возвращает пустой Optional.
     */
    public Optional<Long> resolveUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        try {
            return Optional.of(jwtService.extractUserId(authHeader.substring(7)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
