package by.oma.academy.repository;

import by.oma.academy.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByStepId(Long stepId);
}
