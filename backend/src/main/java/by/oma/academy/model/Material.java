package by.oma.academy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "materials")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private Step step;

    @Column(nullable = false)
    private String name;

    private String sku;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity = BigDecimal.ONE;

    private String unit;

    @Column(name = "price_retail", precision = 10, scale = 2)
    private BigDecimal priceRetail;

    @Column(name = "price_wholesale", precision = 10, scale = 2)
    private BigDecimal priceWholesale;

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

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getPriceRetail() { return priceRetail; }
    public void setPriceRetail(BigDecimal priceRetail) { this.priceRetail = priceRetail; }

    public BigDecimal getPriceWholesale() { return priceWholesale; }
    public void setPriceWholesale(BigDecimal priceWholesale) { this.priceWholesale = priceWholesale; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOmaProductUrl() { return omaProductUrl; }
    public void setOmaProductUrl(String omaProductUrl) { this.omaProductUrl = omaProductUrl; }
}
