package by.oma.academy.controller;

import by.oma.academy.dto.SpendPointsRequest;
import by.oma.academy.dto.SyncCardRequest;
import by.oma.academy.dto.UserPointsDto;
import by.oma.academy.service.AuthHelper;
import by.oma.academy.service.PointsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/points")
public class PointsController {

    private final PointsService pointsService;
    private final AuthHelper authHelper;

    public PointsController(PointsService pointsService, AuthHelper authHelper) {
        this.pointsService = pointsService;
        this.authHelper = authHelper;
    }

    @GetMapping
    public UserPointsDto getMyPoints(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return pointsService.getPoints(userId);
    }

    @PostMapping("/spend")
    public UserPointsDto spendPoints(
            @Valid @RequestBody SpendPointsRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return pointsService.spendPoints(userId, request.points(), request.reason());
    }

    @PostMapping("/sync-card")
    public UserPointsDto syncBonusCard(
            @Valid @RequestBody SyncCardRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return pointsService.syncBonusCard(userId, request.omaBonusCard());
    }
}
