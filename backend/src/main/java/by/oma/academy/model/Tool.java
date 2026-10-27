package by.oma.academy.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tools")
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private Step step;

    @Column(nullable = false)
    private String name;

    private String sku;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "oma_product_url", columnDefinition = "TEXT")
    private String omaProductUrl;

    public Long getId() { return id; }

    public Step getStep() { return step; }
    public void setStep(Step step) { this.step = step; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOmaProductUrl() { return omaProductUrl; }
    public void setOmaProductUrl(String omaProductUrl) { this.omaProductUrl = omaProductUrl; }
}
