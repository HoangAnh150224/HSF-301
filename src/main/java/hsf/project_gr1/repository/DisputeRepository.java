package hsf.project_gr1.repository;

import hsf.project_gr1.model.entity.Dispute;
import hsf.project_gr1.model.enums.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    Optional<Dispute> findByTransactionId(Long transactionId);
    List<Dispute> findByCreatorId(Long creatorId);
    List<Dispute> findByStatus(DisputeStatus status);
}
