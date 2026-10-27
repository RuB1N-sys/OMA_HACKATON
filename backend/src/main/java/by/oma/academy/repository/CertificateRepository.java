package by.oma.academy.repository;

import by.oma.academy.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByUserIdAndProjectId(Long userId, Long projectId);
    List<Certificate> findByUserId(Long userId);
    Optional<Certificate> findByCertNumber(String certNumber);
}
