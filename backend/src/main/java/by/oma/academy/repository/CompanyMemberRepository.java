package by.oma.academy.repository;

import by.oma.academy.model.CompanyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, Long> {
    List<CompanyMember> findByCompanyId(Long companyId);
    List<CompanyMember> findByUserId(Long userId);
    Optional<CompanyMember> findByCompanyIdAndUserId(Long companyId, Long userId);
    boolean existsByCompanyIdAndUserId(Long companyId, Long userId);
}
