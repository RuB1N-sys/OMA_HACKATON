package by.oma.academy.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record AssignCourseRequest(
        @NotNull Long userId,
        @NotNull Long projectId,
        OffsetDateTime deadline
) {}
