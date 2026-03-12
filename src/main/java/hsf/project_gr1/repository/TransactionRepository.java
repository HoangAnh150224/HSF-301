package hsf.project_gr1.repository;

import hsf.project_gr1.model.entity.Transaction;
import hsf.project_gr1.model.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBuyerId(Long buyerId);
    List<Transaction> findByProductId(Long productId);
    Optional<Transaction> findByTransactionCode(String transactionCode);
    List<Transaction> findByStatus(TransactionStatus status);
    List<Transaction> findByProduct_SellerId(Long sellerId);
}
