package by.oma.academy.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGalleryPostRequest(
        Long projectId,
        @NotBlank String title,
        @NotBlank String beforeUrl,
        @NotBlank String afterUrl,
        String description
) {}
