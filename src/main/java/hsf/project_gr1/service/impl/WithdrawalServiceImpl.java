package hsf.project_gr1.service.impl;

import hsf.project_gr1.model.entity.User;
import hsf.project_gr1.model.entity.Wallet;
import hsf.project_gr1.model.entity.Withdrawal;
import hsf.project_gr1.repository.UserRepository;
import hsf.project_gr1.repository.WithdrawalRepository;
import hsf.project_gr1.service.WalletService;
import hsf.project_gr1.service.WithdrawalService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WithdrawalServiceImpl implements WithdrawalService {
    @Autowired
    private  WithdrawalRepository withdrawalRepository;
    @Autowired
    private  WalletService walletService;
    @Autowired
    private  UserRepository userRepository;

    @Transactional
    public void requestWithdrawal(Long userId, BigDecimal amount, String bankName,
                                  String bankAccount, String accountName) {
        if (amount.compareTo(new BigDecimal("10000")) < 0) {
            throw new IllegalArgumentException("So tien toi thieu la 10,000 VND");
        }

        boolean deducted = walletService.deductBalance(userId, amount, "Yeu cau rut tien");
        if (!deducted) {
            throw new IllegalStateException("So du khong du de thuc hien rut tien");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Wallet wallet = walletService.getByUserId(userId);

        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setUser(user);
        withdrawal.setWallet(wallet);
        withdrawal.setAmount(amount);
        withdrawal.setBankName(bankName);
        withdrawal.setBankAccount(bankAccount);
        withdrawal.setAccountName(accountName);

        withdrawalRepository.save(withdrawal);
    }

    public List<Withdrawal> getWithdrawalsByUserId(Long userId) {
        return withdrawalRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    @Override
    public List<Withdrawal> getPendingWithdrawals() {
        return withdrawalRepository.findByStatusOrderByCreatedAtAsc("PENDING");
    }
    @Override
    @Transactional
    public void export(Long id) {
        Withdrawal w = withdrawalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Withdrawal not found"));
        w.setStatus("EXPORTED");
        w.setProcessedAt(LocalDateTime.now());
        withdrawalRepository.save(w);
    }

}
