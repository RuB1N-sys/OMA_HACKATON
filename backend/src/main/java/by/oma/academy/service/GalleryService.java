package by.oma.academy.service;

import by.oma.academy.dto.CreateGalleryPostRequest;
import by.oma.academy.dto.GalleryPostDto;
import by.oma.academy.model.GalleryPost;
import by.oma.academy.model.Project;
import by.oma.academy.model.User;
import by.oma.academy.repository.GalleryPostRepository;
import by.oma.academy.repository.ProjectRepository;
import by.oma.academy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GalleryService {

    private final GalleryPostRepository galleryPostRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final AchievementService achievementService;

    public GalleryService(
            GalleryPostRepository galleryPostRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            AchievementService achievementService
    ) {
        this.galleryPostRepository = galleryPostRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.achievementService = achievementService;
    }

    @Transactional(readOnly = true)
    public List<GalleryPostDto> listPosts() {
        return galleryPostRepository.findAllByOrderByPublishedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public GalleryPostDto createPost(Long userId, CreateGalleryPostRequest request) {
        GalleryPost post = new GalleryPost();
        post.setUserId(userId);
        post.setProjectId(request.projectId());
        post.setTitle(request.title().trim());
        post.setBeforeUrl(request.beforeUrl().trim());
        post.setAfterUrl(request.afterUrl().trim());
        post.setDescription(request.description());

        post = galleryPostRepository.save(post);

        achievementService.checkAndAward(userId);
        return toDto(post);
    }

    private GalleryPostDto toDto(GalleryPost post) {
        String userName = userRepository.findById(post.getUserId())
                .map(User::getName)
                .orElse("Пользователь ОМА");

        String projectTitle = null;
        if (post.getProjectId() != null) {
            projectTitle = projectRepository.findById(post.getProjectId())
                    .map(Project::getTitle)
                    .orElse(null);
        }

        return new GalleryPostDto(
                post.getId(),
                post.getUserId(),
                userName,
                post.getProjectId(),
                projectTitle,
                post.getTitle(),
                post.getBeforeUrl(),
                post.getAfterUrl(),
                post.getDescription(),
                post.getPublishedAt()
        );
    }
}
