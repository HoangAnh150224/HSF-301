package hsf.project_gr1.model.enums;

public enum DisputeStatus {
    PENDING,
    RESOLVED_BUYER_WINS, // Buyer wins
    RESOLVED_SELLER_WINS, // Seller wins
    RESOLVED_MUTUAL, // Mutual agreement (cancelled/refunded without admin judgment or seller refunded)
    CANCELLED
}
