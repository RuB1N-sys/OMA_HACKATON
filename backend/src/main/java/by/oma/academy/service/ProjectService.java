package by.oma.academy.service;

import by.oma.academy.dto.*;
import by.oma.academy.exception.NotFoundException;
import by.oma.academy.model.Material;
import by.oma.academy.model.Project;
import by.oma.academy.model.Step;
import by.oma.academy.model.Tool;
import by.oma.academy.repository.EnrollmentRepository;
import by.oma.academy.repository.ProjectRepository;
import by.oma.academy.repository.StepCompletionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StepCompletionRepository stepCompletionRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            EnrollmentRepository enrollmentRepository,
            StepCompletionRepository stepCompletionRepository
    ) {
        this.projectRepository = projectRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.stepCompletionRepository = stepCompletionRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> listProjects(String audience, String category) {
        List<Project> list;
        if (audience != null && !audience.isBlank() && category != null && !category.isBlank()) {
            list = projectRepository.findByAudienceAndCategory(audience.trim(), category.trim());
        } else if (audience != null && !audience.isBlank()) {
            list = projectRepository.findByAudience(audience.trim());
        } else if (category != null && !category.isBlank()) {
            list = projectRepository.findByCategory(category.trim());
        } else {
            list = projectRepository.findAll();
        }

        return list.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectDetailDto getProject(Long id, Long userId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Проект с ID " + id + " не найден"));

        Set<Long> completedStepIds = Collections.emptySet();
        boolean enrolled = false;
        boolean completed = false;

        if (userId != null) {
            var enrollmentOpt = enrollmentRepository.findByUserIdAndProjectId(userId, id);
            if (enrollmentOpt.isPresent()) {
                enrolled = true;
                completed = "completed".equalsIgnoreCase(enrollmentOpt.get().getStatus());
            }

            List<Long> stepIds = project.getSteps().stream().map(Step::getId).toList();
            if (!stepIds.isEmpty()) {
                completedStepIds = stepCompletionRepository.findByUserIdAndStepIdIn(userId, stepIds).stream()
                        .map(sc -> sc.getStepId())
                        .collect(Collectors.toSet());
            }
        }

        final Set<Long> finalCompletedStepIds = completedStepIds;
        List<StepDto> stepDtos = project.getSteps().stream()
                .sorted(Comparator.comparingInt(Step::getSortOrder))
                .map(s -> toStepDto(s, finalCompletedStepIds.contains(s.getId())))
                .collect(Collectors.toList());

        return new ProjectDetailDto(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getAudience(),
                project.getCategory(),
                project.getDifficulty(),
                project.getDurationMin(),
                project.getPointsReward(),
                project.getPrice(),
                project.getImageUrl(),
                stepDtos,
                enrolled,
                completed
        );
    }

    public ProjectDto toDto(Project p) {
        return new ProjectDto(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getAudience(),
                p.getCategory(),
                p.getDifficulty(),
                p.getDurationMin(),
                p.getPointsReward(),
                p.getPrice(),
                p.getImageUrl(),
                p.getSteps() != null ? p.getSteps().size() : 0
        );
    }

    private StepDto toStepDto(Step s, boolean isCompleted) {
        List<MaterialDto> materials = s.getMaterials() != null ? s.getMaterials().stream()
                .map(this::toMaterialDto)
                .collect(Collectors.toList()) : List.of();

        List<ToolDto> tools = s.getTools() != null ? s.getTools().stream()
                .map(this::toToolDto)
                .collect(Collectors.toList()) : List.of();

        return new StepDto(
                s.getId(),
                s.getStepNumber(),
                s.getTitle(),
                s.getDescription(),
                s.getVideoUrl(),
                s.getSortOrder(),
                materials,
                tools,
                isCompleted
        );
    }

    private MaterialDto toMaterialDto(Material m) {
        return new MaterialDto(
                m.getId(),
                m.getName(),
                m.getSku(),
                m.getQuantity(),
                m.getUnit(),
                m.getPriceRetail(),
                m.getPriceWholesale(),
                m.getImageUrl(),
                m.getOmaProductUrl()
        );
    }

    private ToolDto toToolDto(Tool t) {
        return new ToolDto(
                t.getId(),
                t.getName(),
                t.getSku(),
                t.getImageUrl(),
                t.getOmaProductUrl()
        );
    }
}
