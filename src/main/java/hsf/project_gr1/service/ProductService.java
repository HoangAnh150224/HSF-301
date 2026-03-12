package hsf.project_gr1.service;

import hsf.project_gr1.model.entity.Product;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product createProduct(Product product, Long sellerId);
    Product updateProduct(Long productId, Product productDetails);
    Optional<Product> getProductById(Long id);
    Optional<Product> getProductByCode(String code);
    List<Product> getAllProducts(); // All products (for admin)
    List<Product> getPublicProducts(); // Only ACTIVE products (for public)
    List<Product> getProductsBySeller(Long sellerId);
    String getHiddenContent(Long productId, Long userId);
    boolean verifyProductAccess(Product product, Long userId);
}
