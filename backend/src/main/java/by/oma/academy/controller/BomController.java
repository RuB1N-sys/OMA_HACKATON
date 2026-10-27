package by.oma.academy.controller;

import by.oma.academy.dto.BomResponse;
import by.oma.academy.service.AuthHelper;
import by.oma.academy.service.BomService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bom")
public class BomController {

    private final BomService bomService;
    private final AuthHelper authHelper;

    public BomController(BomService bomService, AuthHelper authHelper) {
        this.bomService = bomService;
        this.authHelper = authHelper;
    }

    @GetMapping
    public BomResponse getBom(
            @RequestParam Long projectId,
            @RequestParam(required = false) Boolean wholesale,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.resolveUserId(authHeader).orElse(null);
        return bomService.getBom(projectId, userId, wholesale);
    }
}
