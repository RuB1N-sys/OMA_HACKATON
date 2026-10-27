package by.oma.academy.dto;

import java.math.BigDecimal;

public record PricingDto(
        Long projectId,
        String projectTitle,
        BigDecimal retailTotal,
        BigDecimal wholesaleTotal,
        BigDecimal userPrice,
        BigDecimal savings,
        boolean isWholesaleEligible,
        String userAudience,
        String omaBonusCard,
        Integer availablePoints
) {}
