package by.oma.academy.controller;

import by.oma.academy.dto.OnboardingDto;
import by.oma.academy.dto.OnboardingRequest;
import by.oma.academy.service.AuthHelper;
import by.oma.academy.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final AuthHelper authHelper;

    public OnboardingController(OnboardingService onboardingService, AuthHelper authHelper) {
        this.onboardingService = onboardingService;
        this.authHelper = authHelper;
    }

    @GetMapping
    public OnboardingDto getOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return onboardingService.getAnswers(userId);
    }

    @PostMapping
    public OnboardingDto saveOnboarding(
            @Valid @RequestBody OnboardingRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return onboardingService.saveAnswers(userId, request);
    }
}
