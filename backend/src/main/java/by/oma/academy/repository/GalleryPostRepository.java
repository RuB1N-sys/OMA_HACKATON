package by.oma.academy.repository;

import by.oma.academy.model.GalleryPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GalleryPostRepository extends JpaRepository<GalleryPost, Long> {
    List<GalleryPost> findAllByOrderByPublishedAtDesc();
    List<GalleryPost> findByUserIdOrderByPublishedAtDesc(Long userId);
    long countByUserId(Long userId);
}
