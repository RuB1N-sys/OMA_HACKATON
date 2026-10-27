package by.oma.academy.repository;

import by.oma.academy.model.OnboardingAnswers;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OnboardingAnswersRepository extends JpaRepository<OnboardingAnswers, Long> {
    Optional<OnboardingAnswers> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
