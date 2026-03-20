package hsf.project_gr1.service;

import hsf.project_gr1.model.entity.Withdrawal;

import java.math.BigDecimal;
import java.util.List;

public interface WithdrawalService {
    public void requestWithdrawal(Long userId, BigDecimal amount, String bankName,
                                  String bankAccount, String accountName);
    public List<Withdrawal> getWithdrawalsByUserId(Long userId);
    List<Withdrawal> getPendingWithdrawals();
    void export(Long id);

}
