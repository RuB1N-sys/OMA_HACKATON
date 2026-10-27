package by.oma.academy.service;

import by.oma.academy.dto.PricingDto;
import by.oma.academy.exception.NotFoundException;
import by.oma.academy.model.Material;
import by.oma.academy.model.Project;
import by.oma.academy.model.Step;
import by.oma.academy.model.User;
import by.oma.academy.model.UserPoints;
import by.oma.academy.repository.ProjectRepository;
import by.oma.academy.repository.UserPointsRepository;
import by.oma.academy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PricingService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserPointsRepository userPointsRepository;

    public PricingService(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            UserPointsRepository userPointsRepository
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.userPointsRepository = userPointsRepository;
    }

    @Transactional(readOnly = true)
    public PricingDto getPricing(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Проект не найден"));

        BigDecimal retailTotal = BigDecimal.ZERO;
        BigDecimal wholesaleTotal = BigDecimal.ZERO;

        if (project.getSteps() != null) {
            for (Step step : project.getSteps()) {
                if (step.getMaterials() != null) {
                    for (Material m : step.getMaterials()) {
                        BigDecimal qty = m.getQuantity() != null ? m.getQuantity() : BigDecimal.ONE;
                        BigDecimal retail = m.getPriceRetail() != null ? m.getPriceRetail() : BigDecimal.ZERO;
                        BigDecimal wholesale = m.getPriceWholesale() != null ? m.getPriceWholesale() : retail;

                        retailTotal = retailTotal.add(retail.multiply(qty));
                        wholesaleTotal = wholesaleTotal.add(wholesale.multiply(qty));
                    }
                }
            }
        }

        boolean isWholesaleEligible = false;
        String userAudience = "b2c";
        String omaBonusCard = null;
        Integer points = 0;

        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                userAudience = user.getAudience();
                if ("pro".equalsIgnoreCase(userAudience) || "team".equalsIgnoreCase(userAudience)) {
                    isWholesaleEligible = true;
                }
            }
            UserPoints up = userPointsRepository.findByUserId(userId).orElse(null);
            if (up != null) {
                omaBonusCard = up.getOmaBonusCard();
                points = up.getTotalPoints();
            }
        }

        BigDecimal userPrice = isWholesaleEligible ? wholesaleTotal : retailTotal;
        BigDecimal savings = retailTotal.subtract(userPrice);
        if (savings.compareTo(BigDecimal.ZERO) < 0) {
            savings = BigDecimal.ZERO;
        }

        return new PricingDto(
                project.getId(),
                project.getTitle(),
                retailTotal.setScale(2, RoundingMode.HALF_UP),
                wholesaleTotal.setScale(2, RoundingMode.HALF_UP),
                userPrice.setScale(2, RoundingMode.HALF_UP),
                savings.setScale(2, RoundingMode.HALF_UP),
                isWholesaleEligible,
                userAudience,
                omaBonusCard,
                points
        );
    }
}
