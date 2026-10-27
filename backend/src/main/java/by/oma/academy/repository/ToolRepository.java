package by.oma.academy.repository;

import by.oma.academy.model.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ToolRepository extends JpaRepository<Tool, Long> {
    List<Tool> findByStepId(Long stepId);
}
