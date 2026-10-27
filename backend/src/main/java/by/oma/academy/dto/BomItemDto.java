package by.oma.academy.dto;

import java.math.BigDecimal;

public record BomItemDto(
        Long materialId,
        String name,
        String sku,
        BigDecimal quantity,
        String unit,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        String priceType,
        String imageUrl,
        String omaProductUrl
) {}
