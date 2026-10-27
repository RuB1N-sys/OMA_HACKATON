package by.oma.academy.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_points")
public class UserPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;

    @Column(name = "oma_bonus_card")
    private String omaBonusCard;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    public Long getId() { return id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

    public String getOmaBonusCard() { return omaBonusCard; }
    public void setOmaBonusCard(String omaBonusCard) { this.omaBonusCard = omaBonusCard; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
