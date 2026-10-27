package by.oma.academy.controller;

import by.oma.academy.dto.AchievementDto;
import by.oma.academy.service.AchievementService;
import by.oma.academy.service.AuthHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    private final AchievementService achievementService;
    private final AuthHelper authHelper;

    public AchievementController(AchievementService achievementService, AuthHelper authHelper) {
        this.achievementService = achievementService;
        this.authHelper = authHelper;
    }

    @GetMapping
    public List<AchievementDto> getMyAchievements(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return achievementService.getUserAchievements(userId);
    }
}
