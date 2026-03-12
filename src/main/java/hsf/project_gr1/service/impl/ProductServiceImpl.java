package hsf.project_gr1.service.impl;

import hsf.project_gr1.model.entity.Product;
import hsf.project_gr1.model.entity.User;
import hsf.project_gr1.model.enums.FeeBearer;
import hsf.project_gr1.model.enums.ProductStatus;
import hsf.project_gr1.model.enums.TransactionStatus;
import hsf.project_gr1.repository.ProductRepository;
import hsf.project_gr1.repository.TransactionRepository;
import hsf.project_gr1.repository.UserRepository;
import hsf.project_gr1.service.ProductService;
import hsf.project_gr1.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final UserRepository userRepository;

    private static final BigDecimal CREATION_FEE = new BigDecimal("5000");

    @Override
    @Transactional
    public Product createProduct(Product product, Long sellerId) {
        // 1. Check Seller Wallet & Deduct Fee
        boolean success = walletService.deductBalance(sellerId, CREATION_FEE, "Fee for creating post: " + product.getTopic());
        if (!success) {
            throw new RuntimeException("Insufficient balance to create post. Fee required: 5000 VND");
        }

        // 2. Set Seller and Defaults
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        
        product.setSeller(seller);
        product.setStatus(ProductStatus.ACTIVE); // Or PENDING if approval needed
        product.setIntermediaryCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Calculate 5% intermediary fee
        BigDecimal intermediaryFee = product.getPrice().multiply(new BigDecimal("0.05"));
        product.setIntermediaryFee(intermediaryFee);

        // Calculate totalPayment based on feeBearer
        // If Buyer bears fee: totalPayment = price + 5%
        // If Seller bears fee: totalPayment = price (seller pays fee from their share)
        if (product.getFeeBearer() == FeeBearer.BUYER) {
            product.setTotalPayment(product.getPrice().add(intermediaryFee));
        } else {
            product.setTotalPayment(product.getPrice());
        }

        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long productId, Product productDetails) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new RuntimeException("Cannot edit product that has been sold or is not active");
        }
        
        // Update logic...
        // Update logic (only update non-null fields)
        if (productDetails.getTopic() != null && !productDetails.getTopic().isEmpty()) product.setTopic(productDetails.getTopic());
        if (productDetails.getDescription() != null) product.setDescription(productDetails.getDescription());
        if (productDetails.getPrice() != null) product.setPrice(productDetails.getPrice());
        if (productDetails.getHiddenContent() != null && !productDetails.getHiddenContent().isEmpty()) product.setHiddenContent(productDetails.getHiddenContent());
        if (productDetails.getVisibility() != null) product.setVisibility(productDetails.getVisibility());
        if (productDetails.getContactMethod() != null) product.setContactMethod(productDetails.getContactMethod());
        
        return productRepository.save(product);
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Optional<Product> getProductByCode(String code) {
        return productRepository.findByIntermediaryCode(code);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getPublicProducts() {
        return productRepository.findByStatusAndVisibility(ProductStatus.ACTIVE, hsf.project_gr1.model.enums.Visibility.PUBLIC);
    }

    @Override
    public List<Product> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    @Override
    public String getHiddenContent(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // 1. If user is Seller -> Allow
        if (product.getSeller().getId().equals(userId)) {
            return product.getHiddenContent();
        }

        // 2. If user is Buyer (Transaction exists) -> Allow if PAID, COMPLETED, or DELIVERED (if applicable)
        // Check if ANY transaction for this product exists where user is buyer and status is valid
        boolean hasBought = transactionRepository.findByProductId(productId).stream()
                .anyMatch(t -> t.getBuyer().getId().equals(userId) && 
                              (t.getStatus() == TransactionStatus.PAID || 
                               t.getStatus() == TransactionStatus.COMPLETED ||
                               t.getStatus() == TransactionStatus.PENDING_PAYMENT)); // Allow PENDING for debugging if needed, but ideally PAID
        
        if (hasBought) {
            return product.getHiddenContent();
        }

        throw new RuntimeException("You do not have permission to view this content");
    }
    @Override
    public boolean verifyProductAccess(Product product, Long userId) {
        if (product.getStatus() == ProductStatus.ACTIVE) {
            return true;
        }

        if (product.getStatus() == ProductStatus.SOLD) {
            // If guest, deny
            if (userId == null) {
                return false;
            }

            // If Seller, allow
            if (product.getSeller().getId().equals(userId)) {
                return true;
            }

            // If Buyer, allow
            // Check transactions
            return transactionRepository.findByProductId(product.getId()).stream()
                    .anyMatch(t -> t.getBuyer().getId().equals(userId) && 
                            (t.getStatus() == TransactionStatus.PAID || 
                             t.getStatus() == TransactionStatus.COMPLETED ||
                             t.getStatus() == TransactionStatus.PENDING_PAYMENT));
        }

        return false; // Other statuses (BANNED, REMOVED) -> Deny
    }
}
