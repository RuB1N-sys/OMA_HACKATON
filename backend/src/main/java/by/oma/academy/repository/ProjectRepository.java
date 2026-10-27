package by.oma.academy.repository;

import by.oma.academy.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByAudience(String audience);
    List<Project> findByCategory(String category);
    List<Project> findByAudienceAndCategory(String audience, String category);
}
