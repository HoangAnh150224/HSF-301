package hsf.project_gr1.service;

import hsf.project_gr1.model.entity.Product;
import hsf.project_gr1.model.entity.Transaction;
import hsf.project_gr1.model.entity.User;
import hsf.project_gr1.model.entity.Wallet;
import hsf.project_gr1.model.enums.FeeBearer;
import hsf.project_gr1.model.enums.ProductStatus;
import hsf.project_gr1.model.enums.TransactionStatus;
import hsf.project_gr1.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
public class TransactionFlowTest {

    @Autowired
    private WalletService walletService;
    @Autowired
    private ProductService productService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private UserRepository userRepository;

    private User seller;
    private User buyer;

    @BeforeEach
    public void setup() {
        // Create Mock Users
        seller = userRepository.findByUsername("seller").orElseGet(() -> 
            userRepository.save(User.builder().username("seller").email("seller@test.com").password("pass").build())
        );
        buyer = userRepository.findByUsername("buyer").orElseGet(() -> 
            userRepository.save(User.builder().username("buyer").email("buyer@test.com").password("pass").build())
        );

        // Create Wallets if not exist
        try { walletService.createWallet(seller.getId()); } catch (Exception e) {}
        try { walletService.createWallet(buyer.getId()); } catch (Exception e) {}
    }

    @Test
    @Transactional
    public void testHappyPathFlow() {
        // 1. Initial Deposit
        walletService.deposit(seller.getId(), new BigDecimal("10000")); // Enough for fee
        walletService.deposit(buyer.getId(), new BigDecimal("100000")); // Enough to buy

        // 2. Create Product
        Product product = Product.builder()
                .topic("Java Course")
                .description("Good course")
                .hiddenContent("SECRET_LINK")
                .price(new BigDecimal("50000"))
                .feeBearer(FeeBearer.SELLER) // Seller pays fee inside price
                .build();
        
        Product createdProduct = productService.createProduct(product, seller.getId());
        
        // Verify Seller Balance (-5000 fee)
        Wallet sellerWallet = walletService.getByUserId(seller.getId());
        Assertions.assertEquals(new BigDecimal("5000.00"), sellerWallet.getBalance());
        Assertions.assertEquals(ProductStatus.ACTIVE, createdProduct.getStatus());

        // 3. Buyer Buys
        Transaction transaction = transactionService.createTransaction(buyer.getId(), createdProduct.getId());
        
        // Verify Buyer Balance (-50000)
        Wallet buyerWallet = walletService.getByUserId(buyer.getId());
        Assertions.assertEquals(new BigDecimal("50000.00"), buyerWallet.getBalance());
        
        Assertions.assertEquals(TransactionStatus.PAID, transaction.getStatus());
        
        // 4. Buyer Confirms
        transactionService.confirmTransaction(transaction.getId(), buyer.getId());
        
        // 5. Verify Final Balances
        // Seller gets: 50000 - 5% (2500) = 47500.
        // Seller Initial: 10000 - 5000 (fee) = 5000.
        // Seller Final: 5000 + 47500 = 52500.
        Wallet finalSellerWallet = walletService.getByUserId(seller.getId());
        Assertions.assertEquals(new BigDecimal("52500.000"), finalSellerWallet.getBalance().stripTrailingZeros()); // Adjust scale if needed, or use compareTo
        
        // Check Transaction Complete
        Transaction finalTx = transactionService.getTransactionById(transaction.getId()).get();
        Assertions.assertEquals(TransactionStatus.COMPLETED, finalTx.getStatus());
    }
}
