package by.oma.academy.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProjectDetailDto(
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
        List<StepDto> steps,
        boolean enrolled,
        boolean completed
) {}
