package hsf.project_gr1.controller.api;

import hsf.project_gr1.model.entity.Transaction;
import hsf.project_gr1.security.CustomUserDetails;
import hsf.project_gr1.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionApiController {

    private final TransactionService transactionService;

    @PostMapping("/buy/{productId}")
    public ResponseEntity<Transaction> buyProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long buyerId = userDetails.getId();
        return ResponseEntity.ok(transactionService.createTransaction(buyerId, productId));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        transactionService.confirmTransaction(id, userId);
        return ResponseEntity.ok("Transaction confirmed");
    }

    @GetMapping
    public ResponseEntity<?> getTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "BUYER") String role) {
        Long userId = userDetails.getId();
        if ("SELLER".equalsIgnoreCase(role)) {
            return ResponseEntity.ok(transactionService.getTransactionsBySeller(userId));
        }
        return ResponseEntity.ok(transactionService.getTransactionsByBuyer(userId));
    }
}
