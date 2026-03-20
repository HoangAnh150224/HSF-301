package hsf.project_gr1.model.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "withdrawals")
public class Withdrawal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false)
    private BigDecimal amount;

    private BigDecimal fee = BigDecimal.ZERO;


    @Column(name = "bank_account", nullable = false)
    private String bankAccount;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }
    @OneToOne(mappedBy = "withdrawal")
    private WalletTransaction walletTransaction;
}
