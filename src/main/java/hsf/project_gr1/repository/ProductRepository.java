package hsf.project_gr1.repository;

import hsf.project_gr1.model.entity.Product;
import hsf.project_gr1.model.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySellerId(Long sellerId);
    List<Product> findByStatus(ProductStatus status);
    
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Product p WHERE p.status = :status AND (p.visibility = :visibility OR p.visibility IS NULL)")
    List<Product> findByStatusAndVisibility(@org.springframework.data.repository.query.Param("status") ProductStatus status, @org.springframework.data.repository.query.Param("visibility") hsf.project_gr1.model.enums.Visibility visibility);

    java.util.Optional<Product> findByIntermediaryCode(String intermediaryCode);
}
