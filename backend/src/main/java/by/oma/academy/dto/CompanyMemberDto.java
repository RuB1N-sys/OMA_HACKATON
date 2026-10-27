package by.oma.academy.dto;

public record CompanyMemberDto(
        Long id,
        Long userId,
        String userName,
        String userEmail,
        String role
) {}
