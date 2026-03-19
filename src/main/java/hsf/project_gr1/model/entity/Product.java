package hsf.project_gr1.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hsf.project_gr1.model.enums.FeeBearer;
import hsf.project_gr1.model.enums.ProductStatus;
import hsf.project_gr1.model.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "intermediary_code", unique = true, nullable = false, length = 30)
    private String intermediaryCode;        // Mã trung gian

    @Column(name = "topic", nullable = false, length = 255)
    private String topic;                   // Chủ đề

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_bearer", nullable = false)
    private FeeBearer feeBearer;

    @Column(name = "intermediary_fee", precision = 15, scale = 2)
    private BigDecimal intermediaryFee;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPayment;

    private String contactMethod;

    @Column(columnDefinition = "TEXT")
    private String hiddenContent;           // Nội dung chỉ hiện sau khi thanh toán

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_status") // Rename to force new column creation to fix ENUM truncation issue
    private Visibility visibility = Visibility.PUBLIC;

    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    @JsonIgnoreProperties({"products", "purchases", "password", "hibernateLazyInitializer", "handler"})
    private User seller;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Quan hệ
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"product", "buyer"})
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"product"})
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<ProductAttachment> attachments = new ArrayList<>();
}