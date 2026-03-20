package hsf.project_gr1.repository;

import hsf.project_gr1.model.entity.WalletTransaction;
import hsf.project_gr1.model.enums.WalletTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByWalletId(Long walletId);
    List<WalletTransaction> findByTransactionCode(String transactionCode);
    Optional<WalletTransaction> findByPaymentTransactionId(String paymentTransactionId);
    Optional<WalletTransaction> findTopByWalletIdAndTypeOrderByCreatedAtDesc(
            Long walletId, WalletTransactionType type);
}
