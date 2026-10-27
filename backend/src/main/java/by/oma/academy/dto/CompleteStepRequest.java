package by.oma.academy.dto;

import jakarta.validation.constraints.NotNull;

public record CompleteStepRequest(
        @NotNull Long stepId
) {}
