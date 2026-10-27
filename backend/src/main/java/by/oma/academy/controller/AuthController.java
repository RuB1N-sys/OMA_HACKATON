package by.oma.academy.controller;

import by.oma.academy.dto.AuthResponse;
import by.oma.academy.dto.LoginRequest;
import by.oma.academy.dto.RegisterRequest;
import by.oma.academy.dto.SsoRequest;
import by.oma.academy.dto.UserDto;
import by.oma.academy.exception.UnauthorizedException;
import by.oma.academy.service.AuthHelper;
import by.oma.academy.service.AuthService;
import by.oma.academy.service.SsoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST-контроллер авторизации и SSO интеграции с oma.by.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SsoService ssoService;
    private final AuthHelper authHelper;

    public AuthController(AuthService authService, SsoService ssoService, AuthHelper authHelper) {
        this.authService = authService;
        this.ssoService = ssoService;
        this.authHelper = authHelper;
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

    @PostMapping("/sso")
    public AuthResponse ssoLogin(@Valid @RequestBody SsoRequest request) {
        return ssoService.ssoLogin(request);
    }

    @GetMapping("/me")
    public UserDto me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = authHelper.requireUserId(authHeader);
        return authService.getProfile(userId);
    }
}
