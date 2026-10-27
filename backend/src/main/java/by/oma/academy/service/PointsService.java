package by.oma.academy.service;

import by.oma.academy.dto.PointsTransactionDto;
import by.oma.academy.dto.UserPointsDto;
import by.oma.academy.exception.BadRequestException;
import by.oma.academy.model.PointsTransaction;
import by.oma.academy.model.UserPoints;
import by.oma.academy.repository.PointsTransactionRepository;
import by.oma.academy.repository.UserPointsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PointsService {

    private final UserPointsRepository userPointsRepository;
    private final PointsTransactionRepository pointsTransactionRepository;

    public PointsService(
            UserPointsRepository userPointsRepository,
            PointsTransactionRepository pointsTransactionRepository
    ) {
        this.userPointsRepository = userPointsRepository;
        this.pointsTransactionRepository = pointsTransactionRepository;
    }

    @Transactional
    public UserPointsDto getPoints(Long userId) {
        UserPoints userPoints = getOrCreateUserPoints(userId);
        List<PointsTransactionDto> transactions = pointsTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toTransactionDto)
                .collect(Collectors.toList());

        return new UserPointsDto(userPoints.getTotalPoints(), userPoints.getOmaBonusCard(), transactions);
    }

    @Transactional
    public void addPoints(Long userId, int amount, String reason, String refType, Long refId) {
        if (amount <= 0) return;

        UserPoints userPoints = getOrCreateUserPoints(userId);
        userPoints.setTotalPoints(userPoints.getTotalPoints() + amount);
        userPointsRepository.save(userPoints);

        PointsTransaction tx = new PointsTransaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setReason(reason);
        tx.setRefType(refType);
        tx.setRefId(refId);
        pointsTransactionRepository.save(tx);
    }

    @Transactional
    public UserPointsDto spendPoints(Long userId, int amount, String reason) {
        if (amount <= 0) {
            throw new BadRequestException("Количество баллов для списания должно быть положительным");
        }

        UserPoints userPoints = getOrCreateUserPoints(userId);
        if (userPoints.getTotalPoints() < amount) {
            throw new BadRequestException("Недостаточно баллов на балансе. Доступно: " + userPoints.getTotalPoints());
        }

        userPoints.setTotalPoints(userPoints.getTotalPoints() - amount);
        userPointsRepository.save(userPoints);

        PointsTransaction tx = new PointsTransaction();
        tx.setUserId(userId);
        tx.setAmount(-amount);
        tx.setReason(reason != null && !reason.isBlank() ? reason : "Списание бонусов на покупку в ОМА");
        tx.setRefType("SPEND");
        tx.setRefId(null);
        pointsTransactionRepository.save(tx);

        return getPoints(userId);
    }

    @Transactional
    public UserPointsDto syncBonusCard(Long userId, String omaBonusCard) {
        if (omaBonusCard == null || omaBonusCard.isBlank()) {
            throw new BadRequestException("Номер бонусной карты ОМА не может быть пустым");
        }

        UserPoints userPoints = getOrCreateUserPoints(userId);
        userPoints.setOmaBonusCard(omaBonusCard.trim());
        userPointsRepository.save(userPoints);

        // При синхронизации карты дарим приветственный бонус лояльности ОМА, если еще не начисляли
        List<PointsTransaction> existing = pointsTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        boolean alreadySynced = existing.stream().anyMatch(t -> "CARD_SYNC".equals(t.getRefType()));
        if (!alreadySynced) {
            addPoints(userId, 50, "Бонус за привязку дисконтной карты ОМА", "CARD_SYNC", null);
        }

        return getPoints(userId);
    }

    private UserPoints getOrCreateUserPoints(Long userId) {
        return userPointsRepository.findByUserId(userId).orElseGet(() -> {
            UserPoints up = new UserPoints();
            up.setUserId(userId);
            up.setTotalPoints(0);
            return userPointsRepository.save(up);
        });
    }

    private PointsTransactionDto toTransactionDto(PointsTransaction tx) {
        return new PointsTransactionDto(
                tx.getId(),
                tx.getAmount(),
                tx.getReason(),
                tx.getRefType(),
                tx.getRefId(),
                tx.getCreatedAt()
        );
    }
}
