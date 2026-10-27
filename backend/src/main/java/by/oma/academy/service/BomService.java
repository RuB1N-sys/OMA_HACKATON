package by.oma.academy.service;

import by.oma.academy.dto.BomItemDto;
import by.oma.academy.dto.BomResponse;
import by.oma.academy.exception.NotFoundException;
import by.oma.academy.model.Material;
import by.oma.academy.model.Project;
import by.oma.academy.model.Step;
import by.oma.academy.model.User;
import by.oma.academy.repository.ProjectRepository;
import by.oma.academy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BomService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public BomService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public BomResponse getBom(Long projectId, Long userId, Boolean forceWholesale) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Проект с ID " + projectId + " не найден"));

        boolean isWholesale = false;
        if (Boolean.TRUE.equals(forceWholesale)) {
            isWholesale = true;
        } else if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && ("pro".equalsIgnoreCase(user.getAudience()) || "team".equalsIgnoreCase(user.getAudience()))) {
                isWholesale = true;
            }
        }

        List<BomItemDto> items = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal retailTotal = BigDecimal.ZERO;
        List<String> cartParams = new ArrayList<>();

        if (project.getSteps() != null) {
            for (Step step : project.getSteps()) {
                if (step.getMaterials() != null) {
                    for (Material m : step.getMaterials()) {
                        BigDecimal qty = m.getQuantity() != null ? m.getQuantity() : BigDecimal.ONE;
                        BigDecimal retail = m.getPriceRetail() != null ? m.getPriceRetail() : BigDecimal.ZERO;
                        BigDecimal wholesale = m.getPriceWholesale() != null ? m.getPriceWholesale() : retail;

                        BigDecimal unitPrice = isWholesale ? wholesale : retail;
                        BigDecimal itemTotal = unitPrice.multiply(qty).setScale(2, RoundingMode.HALF_UP);
                        BigDecimal retailItemTotal = retail.multiply(qty).setScale(2, RoundingMode.HALF_UP);

                        totalPrice = totalPrice.add(itemTotal);
                        retailTotal = retailTotal.add(retailItemTotal);

                        items.add(new BomItemDto(
                                m.getId(),
                                m.getName(),
                                m.getSku(),
                                m.getQuantity(),
                                m.getUnit(),
                                unitPrice,
                                itemTotal,
                                isWholesale ? "wholesale" : "retail",
                                m.getImageUrl(),
                                m.getOmaProductUrl()
                        ));

                        if (m.getSku() != null && !m.getSku().isBlank()) {
                            cartParams.add(m.getSku() + ":" + qty.stripTrailingZeros().toPlainString());
                        }
                    }
                }
            }
        }

        BigDecimal savings = retailTotal.subtract(totalPrice);
        if (savings.compareTo(BigDecimal.ZERO) < 0) {
            savings = BigDecimal.ZERO;
        }

        String cartParamString = String.join(",", cartParams);
        String cartUrl = "https://oma.by/cart?items=" + URLEncoder.encode(cartParamString, StandardCharsets.UTF_8);

        return new BomResponse(
                project.getId(),
                project.getTitle(),
                items,
                totalPrice.setScale(2, RoundingMode.HALF_UP),
                savings.setScale(2, RoundingMode.HALF_UP),
                isWholesale,
                cartUrl
        );
    }
}
