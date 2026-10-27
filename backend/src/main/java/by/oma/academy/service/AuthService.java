package by.oma.academy.service;

import by.oma.academy.dto.AuthResponse;
import by.oma.academy.dto.LoginRequest;
import by.oma.academy.dto.RegisterRequest;
import by.oma.academy.dto.UserDto;
import by.oma.academy.exception.ConflictException;
import by.oma.academy.exception.UnauthorizedException;
import by.oma.academy.model.User;
import by.oma.academy.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Сервис аутентификации: регистрация, вход, получение профиля.
 * Не знает ничего про HTTP — только бизнес-логика.
 */
@Service
public class AuthService {

    private static final Set<String> VALID_AUDIENCES = Set.of("teen", "student", "pro", "team");
    private static final String DEFAULT_AUDIENCE = "student";

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /** Регистрирует нового пользователя и возвращает токен + DTO. */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Данный email уже зарегистрирован");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setAudience(resolveAudience(request.audience()));

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    /** Проверяет учётные данные и возвращает токен + DTO. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Неверный email или пароль"));

        return buildAuthResponse(user);
    }

    /** Возвращает DTO пользователя по его ID. */
    @Transactional(readOnly = true)
    public UserDto getProfile(Long userId) {
        return userRepository.findById(userId)
                .map(this::toDto)
                .orElseThrow(() -> new UnauthorizedException("Пользователь не найден"));
    }

    // -----------------------------------------------------------------------
    // Вспомогательные методы
    // -----------------------------------------------------------------------

    private String resolveAudience(String audience) {
        return (audience != null && VALID_AUDIENCES.contains(audience)) ? audience : DEFAULT_AUDIENCE;
    }

    private AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(jwtService.issue(user), toDto(user));
    }

    private UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getAudience());
    }
}
