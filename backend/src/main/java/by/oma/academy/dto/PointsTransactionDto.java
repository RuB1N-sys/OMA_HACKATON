package by.oma.academy.dto;

import java.time.OffsetDateTime;

public record PointsTransactionDto(
        Long id,
        Integer amount,
        String reason,
        String refType,
        Long refId,
        OffsetDateTime createdAt
) {}
