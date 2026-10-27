package by.oma.academy.service;

import by.oma.academy.dto.AchievementDto;
import by.oma.academy.model.Achievement;
import by.oma.academy.model.Enrollment;
import by.oma.academy.model.UserAchievement;
import by.oma.academy.repository.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CertificateRepository certificateRepository;
    private final GalleryPostRepository galleryPostRepository;
    private final PointsService pointsService;

    public AchievementService(
            AchievementRepository achievementRepository,
            UserAchievementRepository userAchievementRepository,
            EnrollmentRepository enrollmentRepository,
            CertificateRepository certificateRepository,
            GalleryPostRepository galleryPostRepository,
            @Lazy PointsService pointsService
    ) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.certificateRepository = certificateRepository;
        this.galleryPostRepository = galleryPostRepository;
        this.pointsService = pointsService;
    }

    @Transactional(readOnly = true)
    public List<AchievementDto> getUserAchievements(Long userId) {
        List<Achievement> allAchievements = achievementRepository.findAll();
        Map<Long, UserAchievement> earnedMap = userAchievementRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, ua -> ua));

        return allAchievements.stream().map(a -> {
            UserAchievement ua = earnedMap.get(a.getId());
            boolean earned = ua != null;
            return new AchievementDto(
                    a.getId(),
                    a.getCode(),
                    a.getName(),
                    a.getDescription(),
                    a.getIconUrl(),
                    earned,
                    earned ? ua.getEarnedAt() : null
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public void checkAndAward(Long userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
        long completedCount = enrollments.stream()
                .filter(e -> "completed".equalsIgnoreCase(e.getStatus()))
                .count();

        boolean hasB2bCompleted = enrollments.stream()
                .filter(e -> "completed".equalsIgnoreCase(e.getStatus()))
                .anyMatch(e -> "b2b".equalsIgnoreCase(e.getProject().getCategory()));

        boolean hasCertificates = !certificateRepository.findByUserId(userId).isEmpty();
        long galleryPostsCount = galleryPostRepository.countByUserId(userId);

        if (completedCount >= 1) {
            awardIfMissing(userId, "first_project", "Награда за первый пройденный проект ОМА");
        }
        if (completedCount >= 5) {
            awardIfMissing(userId, "five_projects", "Награда за 5 пройденных проектов ОМА");
        }
        if (hasCertificates) {
            awardIfMissing(userId, "certified", "Награда за получение сертификата");
        }
        if (hasB2bCompleted) {
            awardIfMissing(userId, "b2b_pro", "Награда за прохождение B2B курса");
        }
        if (galleryPostsCount >= 1) {
            awardIfMissing(userId, "social_star", "Награда за публикацию в галерее проектов");
        }
    }

    private void awardIfMissing(Long userId, String code, String reason) {
        achievementRepository.findByCode(code).ifPresent(achievement -> {
            if (!userAchievementRepository.existsByUserIdAndAchievementId(userId, achievement.getId())) {
                UserAchievement ua = new UserAchievement();
                ua.setUserId(userId);
                ua.setAchievementId(achievement.getId());
                userAchievementRepository.save(ua);

                pointsService.addPoints(userId, 50, "Достижение: " + achievement.getName(), "ACHIEVEMENT", achievement.getId());
            }
        });
    }
}
