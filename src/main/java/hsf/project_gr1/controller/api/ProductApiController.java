package hsf.project_gr1.controller.api;

import hsf.project_gr1.model.entity.Product;
import hsf.project_gr1.security.CustomUserDetails;
import hsf.project_gr1.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductApiController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long sellerId = userDetails.getId();
        return ResponseEntity.ok(productService.createProduct(product, sellerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Find existing product to check ownership
        Product existing = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (!existing.getSeller().getId().equals(userDetails.getId())) {
             return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        // Return only ACTIVE products for public view
        return ResponseEntity.ok(productService.getPublicProducts());
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<Product> getProduct(@PathVariable String identifier,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Resolve Product
        Optional<Product> productOpt = Optional.empty();
        try {
            Long id = Long.parseLong(identifier);
            productOpt = productService.getProductById(id);
        } catch (NumberFormatException e) {
             // Not a number
        }

        if (productOpt.isEmpty()) {
            productOpt = productService.getProductByCode(identifier);
        }

        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOpt.get();
        Long userId = userDetails != null ? userDetails.getId() : null;

        if (!productService.verifyProductAccess(product, userId)) {
            return ResponseEntity.notFound().build(); // Return 404 to hide existence
        }

        return ResponseEntity.ok(product);
    }

    @GetMapping("/{id}/hidden")
    public ResponseEntity<String> getHiddenContent(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(productService.getHiddenContent(id, userDetails.getId()));
    }

    @GetMapping("/me")
    public ResponseEntity<List<Product>> getMyProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(productService.getProductsBySeller(userDetails.getId()));
    }
}
