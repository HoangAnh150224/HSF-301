package hsf.project_gr1.repository;

import hsf.project_gr1.model.entity.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {
    List<Withdrawal> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Withdrawal> findByStatusOrderByCreatedAtAsc(String status);

}
