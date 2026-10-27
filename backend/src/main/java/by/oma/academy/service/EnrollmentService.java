package by.oma.academy.service;

import by.oma.academy.dto.EnrollmentDto;
import by.oma.academy.exception.BadRequestException;
import by.oma.academy.exception.NotFoundException;
import by.oma.academy.model.Enrollment;
import by.oma.academy.model.Project;
import by.oma.academy.model.Step;
import by.oma.academy.model.StepCompletion;
import by.oma.academy.model.User;
import by.oma.academy.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final ProjectRepository projectRepository;
    private final StepRepository stepRepository;
    private final StepCompletionRepository stepCompletionRepository;
    private final UserRepository userRepository;
    private final PointsService pointsService;
    private final AchievementService achievementService;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            ProjectRepository projectRepository,
            StepRepository stepRepository,
            StepCompletionRepository stepCompletionRepository,
            UserRepository userRepository,
            PointsService pointsService,
            AchievementService achievementService
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.projectRepository = projectRepository;
        this.stepRepository = stepRepository;
        this.stepCompletionRepository = stepCompletionRepository;
        this.userRepository = userRepository;
        this.pointsService = pointsService;
        this.achievementService = achievementService;
    }

    @Transactional
    public EnrollmentDto enroll(Long userId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Проект с ID " + projectId + " не найден"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Enrollment enrollment = enrollmentRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseGet(() -> {
                    Enrollment e = new Enrollment();
                    e.setUser(user);
                    e.setProject(project);
                    e.setStatus("in_progress");
                    return enrollmentRepository.save(e);
                });

        return toDto(enrollment);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDto> getUserEnrollments(Long userId) {
        return enrollmentRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EnrollmentDto getEnrollment(Long userId, Long projectId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new NotFoundException("Запись на проект не найдена"));
        return toDto(enrollment);
    }

    @Transactional
    public EnrollmentDto completeStep(Long userId, Long projectId, Long stepId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Проект не найден"));

        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> new NotFoundException("Шаг с ID " + stepId + " не найден"));

        if (!step.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Указанный шаг не принадлежит данному проекту");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Enrollment enrollment = enrollmentRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseGet(() -> {
                    Enrollment e = new Enrollment();
                    e.setUser(user);
                    e.setProject(project);
                    e.setStatus("in_progress");
                    return enrollmentRepository.save(e);
                });

        if (stepCompletionRepository.findByUserIdAndStepId(userId, stepId).isEmpty()) {
            StepCompletion sc = new StepCompletion();
            sc.setUserId(userId);
            sc.setStepId(stepId);
            stepCompletionRepository.save(sc);
        }

        // Проверяем завершение всех шагов
        List<Step> allSteps = stepRepository.findByProjectIdOrderBySortOrder(projectId);
        List<Long> allStepIds = allSteps.stream().map(Step::getId).toList();
        long completedStepsCount = stepCompletionRepository.countByUserIdAndStepIdIn(userId, allStepIds);

        if (!allStepIds.isEmpty() && completedStepsCount >= allStepIds.size() && !"completed".equalsIgnoreCase(enrollment.getStatus())) {
            enrollment.setStatus("completed");
            enrollment.setCompletedAt(OffsetDateTime.now());
            enrollmentRepository.save(enrollment);

            // Начисляем баллы за завершение проекта
            if (project.getPointsReward() > 0) {
                pointsService.addPoints(
                        userId,
                        project.getPointsReward(),
                        "Успешное завершение проекта: " + project.getTitle(),
                        "PROJECT",
                        project.getId()
                );
            }

            // Проверяем достижения
            achievementService.checkAndAward(userId);
        }

        return toDto(enrollment);
    }

    private EnrollmentDto toDto(Enrollment e) {
        Project p = e.getProject();
        List<Step> allSteps = stepRepository.findByProjectIdOrderBySortOrder(p.getId());
        List<Long> stepIds = allSteps.stream().map(Step::getId).toList();
        int completedSteps = stepIds.isEmpty() ? 0 :
                (int) stepCompletionRepository.countByUserIdAndStepIdIn(e.getUser().getId(), stepIds);

        return new EnrollmentDto(
                e.getId(),
                p.getId(),
                p.getTitle(),
                e.getStatus(),
                e.getEnrolledAt(),
                e.getCompletedAt(),
                completedSteps,
                allSteps.size()
        );
    }
}
