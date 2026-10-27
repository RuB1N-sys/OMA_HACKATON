package by.oma.academy.dto;

import java.math.BigDecimal;

public record MaterialDto(
        Long id,
        String name,
        String sku,
        BigDecimal quantity,
        String unit,
        BigDecimal priceRetail,
        BigDecimal priceWholesale,
        String imageUrl,
        String omaProductUrl
) {}
