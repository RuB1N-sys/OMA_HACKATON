package by.oma.academy.dto;

import java.time.OffsetDateTime;

public record GalleryPostDto(
        Long id,
        Long userId,
        String userName,
        Long projectId,
        String projectTitle,
        String title,
        String beforeUrl,
        String afterUrl,
        String description,
        OffsetDateTime publishedAt
) {}
