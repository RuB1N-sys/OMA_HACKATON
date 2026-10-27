package by.oma.academy;

import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Set<String> AUDIENCES = Set.of("teen", "student", "pro", "team");

    record RegisterReq(
            @NotBlank(message = "Введите имя") @Size(max = 100, message = "Имя слишком длинное") String name,
            @NotBlank(message = "Введите email") @Email(message = "Некорректный email") @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 72, message = "Пароль: от 8 до 72 символов") String password,
            String audience) {}
    record LoginReq(@NotBlank(message = "Введите email") String email, @NotBlank(message = "Введите пароль") String password) {}
    record UserDto(Long id, String name, String email, String audience) {}
    record AuthRes(String token, UserDto user) {}

    private final UserRepository users;
    private final JwtService jwt;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository users, JwtService jwt) { this.users = users; this.jwt = jwt; }

    private static UserDto dto(User u) { return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getAudience()); }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthRes register(@Valid @RequestBody RegisterReq r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Этот email уже зарегистрирован");
        User u = new User();
        u.setName(r.name().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(r.password()));
        // Сотрудников ОМА нужно подтверждать отдельно (например, по корпоративной почте), здесь это не делается.
        u.setAudience(r.audience() != null && AUDIENCES.contains(r.audience()) ? r.audience() : "student");
        u = users.save(u);
        return new AuthRes(jwt.issue(u), dto(u));
    }

    @PostMapping("/login")
    public AuthRes login(@Valid @RequestBody LoginReq r) {
        User u = users.findByEmailIgnoreCase(r.email().trim()).orElse(null);
        if (u == null || !encoder.matches(r.password(), u.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль");
        return new AuthRes(jwt.issue(u), dto(u));
    }

    @GetMapping("/me")
    public UserDto me(@RequestHeader(value = "Authorization", required = false) String h) {
        if (h == null || !h.startsWith("Bearer "))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Требуется вход");
        try {
            return users.findById(jwt.userId(h.substring(7))).map(AuthController::dto)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        } catch (JwtException | NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Сессия истекла");
        }
    }
}
