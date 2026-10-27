package by.oma.academy.dto;

import java.math.BigDecimal;
import java.util.List;

public record BomResponse(
        Long projectId,
        String projectTitle,
        List<BomItemDto> items,
        BigDecimal totalPrice,
        BigDecimal totalSavings,
        boolean wholesaleDiscountApplied,
        String cartUrl
) {}
