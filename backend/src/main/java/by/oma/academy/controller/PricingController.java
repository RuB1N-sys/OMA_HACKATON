package by.oma.academy.controller;

import by.oma.academy.dto.PricingDto;
import by.oma.academy.service.AuthHelper;
import by.oma.academy.service.PricingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private final PricingService pricingService;
    private final AuthHelper authHelper;

    public PricingController(PricingService pricingService, AuthHelper authHelper) {
        this.pricingService = pricingService;
        this.authHelper = authHelper;
    }

    @GetMapping
    public PricingDto getPricing(
            @RequestParam Long projectId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.resolveUserId(authHeader).orElse(null);
        return pricingService.getPricing(projectId, userId);
    }
}
