package by.oma.academy.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "onboarding_answers")
public class OnboardingAnswers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /** Stored as JSON string e.g. ["art","design"] */
    @Column(columnDefinition = "TEXT")
    private String interests;

    private String experience;

    /** Stored as JSON string e.g. ["learn","grow"] */
    @Column(columnDefinition = "TEXT")
    private String goals;

    @Column(name = "answered_at", insertable = false, updatable = false)
    private OffsetDateTime answeredAt;

    public Long getId() { return id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = interests; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getGoals() { return goals; }
    public void setGoals(String goals) { this.goals = goals; }

    public OffsetDateTime getAnsweredAt() { return answeredAt; }
}
