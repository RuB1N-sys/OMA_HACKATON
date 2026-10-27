package by.oma.academy.dto;

import java.util.List;

public record OnboardingDto(
        List<String> interests,
        String experience,
        List<String> goals,
        boolean completed
) {}
