package by.oma.academy.dto;

import java.util.List;

public record UserPointsDto(
        Integer totalPoints,
        String omaBonusCard,
        List<PointsTransactionDto> transactions
) {}
