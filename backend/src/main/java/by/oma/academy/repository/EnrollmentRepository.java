package by.oma.academy.repository;

import by.oma.academy.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByUserIdAndProjectId(Long userId, Long projectId);
    List<Enrollment> findByUserId(Long userId);
}
