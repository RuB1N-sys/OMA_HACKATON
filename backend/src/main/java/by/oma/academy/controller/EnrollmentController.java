package by.oma.academy.controller;

import by.oma.academy.dto.EnrollRequest;
import by.oma.academy.dto.EnrollmentDto;
import by.oma.academy.service.AuthHelper;
import by.oma.academy.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final AuthHelper authHelper;

    public EnrollmentController(EnrollmentService enrollmentService, AuthHelper authHelper) {
        this.enrollmentService = enrollmentService;
        this.authHelper = authHelper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentDto enroll(
            @Valid @RequestBody EnrollRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return enrollmentService.enroll(userId, request.projectId());
    }

    @GetMapping
    public List<EnrollmentDto> getMyEnrollments(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return enrollmentService.getUserEnrollments(userId);
    }

    @GetMapping("/{projectId}")
    public EnrollmentDto getEnrollment(
            @PathVariable Long projectId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return enrollmentService.getEnrollment(userId, projectId);
    }

    @PostMapping("/{projectId}/steps/{stepId}/complete")
    public EnrollmentDto completeStep(
            @PathVariable Long projectId,
            @PathVariable Long stepId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return enrollmentService.completeStep(userId, projectId, stepId);
    }
}
