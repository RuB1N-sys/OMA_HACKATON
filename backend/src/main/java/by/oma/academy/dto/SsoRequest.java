package by.oma.academy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SsoRequest(
        @NotBlank @Email String email,
        @NotBlank String name,
        String omaBonusCard,
        String omaSsoToken,
        String audience
) {}
