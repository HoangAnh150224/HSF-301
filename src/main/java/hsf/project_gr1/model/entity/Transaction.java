package hsf.project_gr1.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hsf.project_gr1.model.enums.FeeBearer;
import hsf.project_gr1.model.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String transactionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    @JsonIgnoreProperties({"products", "purchases", "password", "hibernateLazyInitializer", "handler"})
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"transactions", "seller", "attachments", "hibernateLazyInitializer", "handler"})
    private Product product;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal intermediaryFee;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeeBearer feeBearer;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.PENDING_PAYMENT;

    private String paymentMethod;
    private String paymentTransactionId;    // ID từ VNPay/Momo/...

    private LocalDateTime paidAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime completedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
