package by.oma.academy.service;

import by.oma.academy.dto.AuthResponse;
import by.oma.academy.dto.SsoRequest;
import by.oma.academy.dto.UserDto;
import by.oma.academy.model.User;
import by.oma.academy.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SsoService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PointsService pointsService;
    private final BCryptPasswordEncoder passwordEncoder;

    public SsoService(
            UserRepository userRepository,
            JwtService jwtService,
            PointsService pointsService
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.pointsService = pointsService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Бесшовный вход / синхронизация через единую учетную запись oma.by SSO.
     */
    @Transactional
    public AuthResponse ssoLogin(SsoRequest request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(request.name().trim());
            newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            String aud = request.audience() != null && !request.audience().isBlank() ? request.audience() : "student";
            newUser.setAudience(aud);
            return userRepository.save(newUser);
        });

        // Синхронизируем бонусную карту ОМА
        if (request.omaBonusCard() != null && !request.omaBonusCard().isBlank()) {
            pointsService.syncBonusCard(user.getId(), request.omaBonusCard());
        }

        String token = jwtService.issue(user);
        UserDto userDto = new UserDto(user.getId(), user.getName(), user.getEmail(), user.getAudience());
        return new AuthResponse(token, userDto);
    }
}
