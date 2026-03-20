package hsf.project_gr1.service;

import hsf.project_gr1.model.entity.Wallet;
import hsf.project_gr1.model.entity.Withdrawal;

import java.math.BigDecimal;

public interface WalletService {
    Wallet getByUserId(Long userId);
    Wallet createWallet(Long userId);
    void deposit(Long userId, BigDecimal amount);
    boolean deductBalance(Long userId, BigDecimal amount, String description);
    void addBalance(Long userId, BigDecimal amount, String description);
    void linkWithdrawalToLastTransaction(Long userId, Withdrawal withdrawal);

}
