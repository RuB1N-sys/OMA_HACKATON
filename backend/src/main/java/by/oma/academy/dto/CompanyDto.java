package by.oma.academy.dto;

import java.time.OffsetDateTime;

public record CompanyDto(
        Long id,
        String name,
        Long ownerId,
        String ownerName,
        OffsetDateTime createdAt,
        int membersCount
) {}
