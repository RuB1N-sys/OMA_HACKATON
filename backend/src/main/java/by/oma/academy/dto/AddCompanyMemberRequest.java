package by.oma.academy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddCompanyMemberRequest(
        @NotBlank @Email String email,
        String role
) {}
