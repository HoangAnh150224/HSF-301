package hsf.project_gr1.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class PageController {

    private final hsf.project_gr1.service.ProductService productService;

    @GetMapping
    public String home() {
        return "index"; // landing page
    }

    @GetMapping("/products")
    public String listProducts() {
        return "products/list";
    }

    @GetMapping("/my-products")
    public String myProducts() {
        return "products/my-products";
    }

    @GetMapping("/products/{identifier}")
    public String productDetail(@PathVariable String identifier, Model model, 
                                @org.springframework.security.core.annotation.AuthenticationPrincipal hsf.project_gr1.security.CustomUserDetails userDetails) {
        // Resolve Product
        java.util.Optional<hsf.project_gr1.model.entity.Product> productOpt = java.util.Optional.empty();
        try {
            Long id = Long.parseLong(identifier);
            productOpt = productService.getProductById(id);
        } catch (NumberFormatException e) {
            productOpt = productService.getProductByCode(identifier);
        }

        if (productOpt.isEmpty()) {
             // If not found, let frontend decide or 404 here. 
             // Ideally 404 here is better.
             throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Product not found");
        }

        hsf.project_gr1.model.entity.Product product = productOpt.get();
        Long currentUserId = userDetails != null ? userDetails.getId() : null;

        // CHECK ACCESS using Service
        if (!productService.verifyProductAccess(product, currentUserId)) {
             throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Product not found or access denied");
        }

        System.out.println("Accessing product detail for: " + identifier);
        model.addAttribute("productId", identifier);
        return "products/detail";
    }

    @GetMapping("/products/create")
    public String createProduct() {
        return "products/create";
    }

    @GetMapping("/products/{identifier}/edit")
    public String editProduct(@PathVariable String identifier, Model model, 
                              @org.springframework.security.core.annotation.AuthenticationPrincipal hsf.project_gr1.security.CustomUserDetails userDetails) {
        
        // 1. Resolve Product
        java.util.Optional<hsf.project_gr1.model.entity.Product> productOpt = java.util.Optional.empty();
        try {
            Long id = Long.parseLong(identifier);
            productOpt = productService.getProductById(id);
        } catch (NumberFormatException e) {
            productOpt = productService.getProductByCode(identifier);
        }

        if (productOpt.isEmpty()) {
            return "redirect:/products"; // Or 404
        }

        hsf.project_gr1.model.entity.Product product = productOpt.get();
        Long currentUserId = userDetails != null ? userDetails.getId() : null;

        // NEW: Check Access for SOLD items
        if (!productService.verifyProductAccess(product, currentUserId)) {
             throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Product not found or access denied");
        }

        // 2. Check Ownership (Existing logic for EDIT page is separate, keep strict there)
        // But wait, owner is handled in verifyProductAccess (returns true).
        // This method below is `editProduct`.
        // `editProduct` MUST be Owner Only.
        // So verifyProductAccess is loose (Buyer OK).
        // But Edit requires Seller.
        
        // Correct logic for EDIT page:
        if (userDetails == null || !product.getSeller().getId().equals(userDetails.getId())) {
             // If they are allowed to View (e.g. Buyer), they go to Detail.
             // If they are NOT allowed (Stranger + Sold), verifyProductAccess would have thrown 404.
             // So here we redirect to Detail.
             return "redirect:/products/" + identifier; 
        }

        model.addAttribute("productId", identifier);
        return "products/edit";
    }

    @GetMapping("/wallet")
    public String wallet() {
        return "wallet/index";
    }

    @GetMapping("/transactions")
    public String transactions() {
        return "transactions/history";
    }
}
