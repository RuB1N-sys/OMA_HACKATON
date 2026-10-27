package by.oma.academy.repository;

import by.oma.academy.model.CourseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, Long> {
    List<CourseAssignment> findByCompanyId(Long companyId);
    List<CourseAssignment> findByUserId(Long userId);
    Optional<CourseAssignment> findByCompanyIdAndUserIdAndProjectId(Long companyId, Long userId, Long projectId);
}
