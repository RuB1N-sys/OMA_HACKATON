package by.oma.academy.controller;

import by.oma.academy.dto.AuthResponse;
import by.oma.academy.dto.LoginRequest;
import by.oma.academy.dto.RegisterRequest;
import by.oma.academy.dto.UserDto;
import by.oma.academy.exception.UnauthorizedException;
import by.oma.academy.service.AuthService;
import by.oma.academy.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST-контроллер аутентификации.
 * Отвечает только за HTTP: разбор запросов, вызов сервиса, формирование ответов.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserDto me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Токен отсутствует");
        }
        try {
            Long userId = jwtService.extractUserId(authHeader.substring(7));
            return authService.getProfile(userId);
        } catch (JwtException | NumberFormatException e) {
            throw new UnauthorizedException("Недействительный токен");
        }
    }
}
