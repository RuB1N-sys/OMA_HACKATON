package by.oma.academy.dto;

import java.math.BigDecimal;

public record ProjectDto(
        Long id,
        String title,
        String description,
        String audience,
        String category,
        String difficulty,
        Integer durationMin,
        Integer pointsReward,
        BigDecimal price,
        String imageUrl,
        int stepsCount
) {}
