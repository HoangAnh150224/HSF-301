package hsf.project_gr1.service;

import hsf.project_gr1.model.entity.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionService {
    Transaction createTransaction(Long buyerId, Long productId);
    void confirmTransaction(Long transactionId, Long userId); // userId to verify permission (Buyer usually confirms)
    void cancelTransaction(Long transactionId, Long userId); // Only if not paid or other logic
    Optional<Transaction> getTransactionById(Long id);
    List<Transaction> getTransactionsByBuyer(Long buyerId);
    List<Transaction> getTransactionsBySeller(Long sellerId); // Derived from products
}
