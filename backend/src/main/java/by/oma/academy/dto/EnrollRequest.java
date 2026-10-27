package by.oma.academy.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollRequest(
        @NotNull Long projectId
) {}
