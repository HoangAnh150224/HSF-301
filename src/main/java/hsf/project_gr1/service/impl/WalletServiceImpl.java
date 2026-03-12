package hsf.project_gr1.service.impl;

import hsf.project_gr1.model.entity.User;
import hsf.project_gr1.model.entity.Wallet;
import hsf.project_gr1.model.entity.WalletTransaction;
import hsf.project_gr1.model.enums.WalletTransactionStatus;
import hsf.project_gr1.model.enums.WalletTransactionType;
import hsf.project_gr1.repository.UserRepository;
import hsf.project_gr1.repository.WalletRepository;
import hsf.project_gr1.repository.WalletTransactionRepository;
import hsf.project_gr1.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public Wallet getByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user id: " + userId));
    }

    @Override
    @Transactional
    public Wallet createWallet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (walletRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("Wallet already exists");
        }

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .build();
        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public void deposit(Long userId, BigDecimal amount) {
        Wallet wallet = getByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        createTransaction(wallet, amount, WalletTransactionType.DEPOSIT, "Deposit money", WalletTransactionStatus.COMPLETED);
    }

    @Override
    @Transactional
    public boolean deductBalance(Long userId, BigDecimal amount, String description) {
        Wallet wallet = getByUserId(userId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            return false;
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        createTransaction(wallet, amount.negate(), WalletTransactionType.WITHDRAW, description, WalletTransactionStatus.COMPLETED);
        return true;
    }

    @Override
    @Transactional
    public void addBalance(Long userId, BigDecimal amount, String description) {
        Wallet wallet = getByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        createTransaction(wallet, amount, WalletTransactionType.REFUND, description, WalletTransactionStatus.COMPLETED);
    }

    private void createTransaction(Wallet wallet, BigDecimal amount, WalletTransactionType type, String description, WalletTransactionStatus status) {
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(type)
                .transactionCode(UUID.randomUUID().toString().substring(0, 30))
                .description(description)
                .status(status)
                .build();
        transactionRepository.save(transaction);
    }
}
