package by.oma.academy.controller;

import by.oma.academy.dto.*;
import by.oma.academy.service.AuthHelper;
import by.oma.academy.service.B2bService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/b2b")
public class B2bController {

    private final B2bService b2bService;
    private final AuthHelper authHelper;

    public B2bController(B2bService b2bService, AuthHelper authHelper) {
        this.b2bService = b2bService;
        this.authHelper = authHelper;
    }

    @PostMapping("/companies")
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyDto createCompany(
            @Valid @RequestBody CreateCompanyRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return b2bService.createCompany(userId, request);
    }

    @GetMapping("/companies")
    public List<CompanyDto> getMyCompanies(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return b2bService.getUserCompanies(userId);
    }

    @GetMapping("/companies/{id}")
    public CompanyDto getCompany(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return b2bService.getCompany(id, userId);
    }

    @PostMapping("/companies/{id}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyMemberDto addMember(
            @PathVariable Long id,
            @Valid @RequestBody AddCompanyMemberRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return b2bService.addMember(id, userId, request);
    }

    @GetMapping("/companies/{id}/members")
    public List<CompanyMemberDto> listMembers(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return b2bService.listMembers(id, userId);
    }

    @PostMapping("/companies/{id}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseAssignmentDto assignCourse(
            @PathVariable Long id,
            @Valid @RequestBody AssignCourseRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return b2bService.assignCourse(id, userId, request);
    }

    @GetMapping("/companies/{id}/assignments")
    public List<CourseAssignmentDto> listAssignments(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return b2bService.listAssignments(id, userId);
    }
}
