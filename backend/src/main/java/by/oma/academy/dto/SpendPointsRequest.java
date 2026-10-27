package by.oma.academy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SpendPointsRequest(
        @NotNull @Min(1) Integer points,
        @NotBlank String reason
) {}
