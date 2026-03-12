package hsf.project_gr1.service;

import hsf.project_gr1.model.entity.Dispute;
import hsf.project_gr1.model.enums.DisputeStatus;
import java.util.Optional;

public interface DisputeService {
    Dispute createDispute(Long transactionId, Long creatorId, String reason);
    Dispute resolveDispute(Long disputeId, DisputeStatus resolution, String adminComment); // Admin only
    Optional<Dispute> getDisputeById(Long id);
    java.util.List<Dispute> getAllDisputes();
}
