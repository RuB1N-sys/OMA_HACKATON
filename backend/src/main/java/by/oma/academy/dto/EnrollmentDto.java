package by.oma.academy.dto;

import java.time.OffsetDateTime;

public record EnrollmentDto(
        Long id,
        Long projectId,
        String projectTitle,
        String status,
        OffsetDateTime enrolledAt,
        OffsetDateTime completedAt,
        int completedSteps,
        int totalSteps
) {}
