package by.oma.academy.dto;

import java.time.OffsetDateTime;

public record CourseAssignmentDto(
        Long id,
        Long companyId,
        Long userId,
        String userName,
        Long projectId,
        String projectTitle,
        Long assignedBy,
        OffsetDateTime assignedAt,
        OffsetDateTime deadline,
        String status,
        int completedSteps,
        int totalSteps
) {}
