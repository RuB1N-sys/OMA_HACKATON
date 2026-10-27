package by.oma.academy.repository;

import by.oma.academy.model.Step;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StepRepository extends JpaRepository<Step, Long> {
    List<Step> findByProjectIdOrderBySortOrder(Long projectId);
}
