package by.oma.academy.dto;

import java.time.OffsetDateTime;

public record AchievementDto(
        Long id,
        String code,
        String name,
        String description,
        String iconUrl,
        boolean earned,
        OffsetDateTime earnedAt
) {}
