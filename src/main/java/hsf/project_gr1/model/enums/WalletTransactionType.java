package hsf.project_gr1.model.enums;

public enum WalletTransactionType {
    DEPOSIT,    // Nạp tiền
    WITHDRAW,   // Rút tiền
    TRANSFER,   // Chuyển tiền (nếu hỗ trợ giữa users)
    REFUND      // Hoàn tiền từ giao dịch
}
