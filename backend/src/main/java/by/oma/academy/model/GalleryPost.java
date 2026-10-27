package by.oma.academy.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "gallery_posts")
public class GalleryPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "project_id")
    private Long projectId;

    private String title;

    @Column(name = "before_url", columnDefinition = "TEXT")
    private String beforeUrl;

    @Column(name = "after_url", columnDefinition = "TEXT")
    private String afterUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "published_at", insertable = false, updatable = false)
    private OffsetDateTime publishedAt;

    public Long getId() { return id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBeforeUrl() { return beforeUrl; }
    public void setBeforeUrl(String beforeUrl) { this.beforeUrl = beforeUrl; }

    public String getAfterUrl() { return afterUrl; }
    public void setAfterUrl(String afterUrl) { this.afterUrl = afterUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public OffsetDateTime getPublishedAt() { return publishedAt; }
}
