package hsf.project_gr1.repository;

import hsf.project_gr1.model.entity.ProductAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductAttachmentRepository extends JpaRepository<ProductAttachment, Long> {
    List<ProductAttachment> findByProductIdAndIsHidden(Long productId, Boolean isHidden);
    List<ProductAttachment> findByProductId(Long productId);
}
