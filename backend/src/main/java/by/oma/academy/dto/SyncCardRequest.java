package by.oma.academy.dto;

import jakarta.validation.constraints.NotBlank;

public record SyncCardRequest(
        @NotBlank String omaBonusCard
) {}
