package by.oma.academy.controller;

import by.oma.academy.dto.ProjectDetailDto;
import by.oma.academy.dto.ProjectDto;
import by.oma.academy.service.AuthHelper;
import by.oma.academy.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final AuthHelper authHelper;

    public ProjectController(ProjectService projectService, AuthHelper authHelper) {
        this.projectService = projectService;
        this.authHelper = authHelper;
    }

    @GetMapping
    public List<ProjectDto> listProjects(
            @RequestParam(required = false) String audience,
            @RequestParam(required = false) String category
    ) {
        return projectService.listProjects(audience, category);
    }

    @GetMapping("/{id}")
    public ProjectDetailDto getProject(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.resolveUserId(authHeader).orElse(null);
        return projectService.getProject(id, userId);
    }
}
