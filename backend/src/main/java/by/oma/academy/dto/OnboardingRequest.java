package by.oma.academy.dto;

import java.util.List;

public record OnboardingRequest(
        List<String> interests,
        String experience,
        List<String> goals
) {}
