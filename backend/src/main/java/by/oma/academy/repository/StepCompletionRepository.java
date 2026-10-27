package by.oma.academy.repository;

import by.oma.academy.model.StepCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StepCompletionRepository extends JpaRepository<StepCompletion, Long> {
    List<StepCompletion> findByUserIdAndStepIdIn(Long userId, Collection<Long> stepIds);
    long countByUserIdAndStepIdIn(Long userId, Collection<Long> stepIds);
    Optional<StepCompletion> findByUserIdAndStepId(Long userId, Long stepId);
}
