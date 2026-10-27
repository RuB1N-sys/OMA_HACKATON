package by.oma.academy.service;

import by.oma.academy.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Сервис для выпуска и разбора JWT-токенов.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long ttlMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.ttl-hours}") long hours
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlMs = hours * 3_600_000L;
    }

    /** Выпускает токен для заданного пользователя. */
    public String issue(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlMs))
                .signWith(key)
                .compact();
    }

    /**
     * Извлекает ID пользователя из токена.
     * Бросает JwtException, если токен недействителен или просрочен.
     */
    public Long extractUserId(String token) {
        return Long.valueOf(
                Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getSubject()
        );
    }
}
