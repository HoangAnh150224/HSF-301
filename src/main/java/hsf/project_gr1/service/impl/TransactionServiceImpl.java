package hsf.project_gr1.service.impl;

import hsf.project_gr1.model.entity.Product;
import hsf.project_gr1.model.entity.Transaction;
import hsf.project_gr1.model.entity.User;
import hsf.project_gr1.model.enums.FeeBearer;
import hsf.project_gr1.model.enums.ProductStatus;
import hsf.project_gr1.model.enums.TransactionStatus;
import hsf.project_gr1.repository.ProductRepository;
import hsf.project_gr1.repository.TransactionRepository;
import hsf.project_gr1.repository.UserRepository;
import hsf.project_gr1.service.TransactionService;
import hsf.project_gr1.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final WalletService walletService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Transaction createTransaction(Long buyerId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getStatus().equals(ProductStatus.ACTIVE)) {
             throw new RuntimeException("Product is not available for sale");
        }

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));
        
        if (product.getSeller().getId().equals(buyerId)) {
            throw new RuntimeException("Cannot buy your own product");
        }

        // Calculate amounts
        BigDecimal price = product.getPrice();
        BigDecimal fee = price.multiply(new BigDecimal("0.05"));
        BigDecimal totalAmount;

        if (product.getFeeBearer() == FeeBearer.BUYER) {
            totalAmount = price.add(fee);
        } else {
            totalAmount = price;
        }

        // Deduct from Buyer
        boolean success = walletService.deductBalance(buyerId, totalAmount, "Payment for product: " + product.getTopic());
        if (!success) {
            throw new RuntimeException("Insufficient balance");
        }

        // Create Transaction
        Transaction transaction = Transaction.builder()
                .transactionCode(UUID.randomUUID().toString().substring(0, 30))
                .buyer(buyer)
                .product(product)
                .price(price)
                .intermediaryFee(fee)
                .totalAmount(totalAmount)
                .feeBearer(product.getFeeBearer())
                .status(TransactionStatus.PAID) // Money held by system
                .paidAt(LocalDateTime.now())
                .build();
        
        // Update Product Status
        product.setStatus(ProductStatus.SOLD); // Or PENDING_DELIVERY? 'SOLD' implies done? Let's use SOLD or create RESERVED
        // For now, let's use SOLD to block others.
        productRepository.save(product);

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void confirmTransaction(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Verify user is buyer? (Only Buyer can confirm receipt)
        if (!transaction.getBuyer().getId().equals(userId)) {
            // Or Admin?
            // throw new RuntimeException("Only buyer can confirm");
        }

        if (transaction.getStatus() != TransactionStatus.PAID) {
             throw new RuntimeException("Transaction not in valid state to confirm"); // Only PAID (Held) can be confirmed
        }

        // Release Money to Seller
        BigDecimal sellerAmount;
        if (transaction.getFeeBearer() == FeeBearer.BUYER) {
            // Buyer paid Price + Fee. Seller gets Price.
            sellerAmount = transaction.getPrice();
        } else {
            // Buyer paid Price. Seller gets Price - Fee.
            sellerAmount = transaction.getPrice().subtract(transaction.getIntermediaryFee());
        }

        walletService.addBalance(transaction.getProduct().getSeller().getId(), sellerAmount, "Sale revenue for: " + transaction.getProduct().getTopic());

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void cancelTransaction(Long transactionId, Long userId) {
        // Logic for cancellation (Refund buyer)
        // ...
    }

    @Override
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    @Override
    public List<Transaction> getTransactionsByBuyer(Long buyerId) {
        return transactionRepository.findByBuyerId(buyerId);
    }

    @Override
    public List<Transaction> getTransactionsBySeller(Long sellerId) {
        return transactionRepository.findByProduct_SellerId(sellerId);
    }
}
