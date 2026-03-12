package hsf.project_gr1.controller.api;

import hsf.project_gr1.model.entity.User;
import hsf.project_gr1.model.entity.Wallet;
import hsf.project_gr1.repository.UserRepository;
import hsf.project_gr1.service.PayOSPaymentService;
import hsf.project_gr1.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletApiController {

    private final WalletService walletService;
    private final PayOSPaymentService payOSPaymentService;
    private final UserRepository userRepository;

    @GetMapping("/balance")
    public ResponseEntity<Wallet> getBalance(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(walletService.getByUserId(user.getId()));
    }

    @PostMapping("/deposit")
    public ResponseEntity<Map<String, String>> deposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam BigDecimal amount) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            String checkoutUrl = payOSPaymentService.createPaymentLink(amount, user.getId());

            Map<String, String> response = new HashMap<>();
            response.put("paymentUrl", checkoutUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to create PayOS payment link", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không thể tạo link thanh toán: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/payos-confirm")
    public ResponseEntity<Map<String, Object>> confirmPayOSPayment(@RequestParam long orderCode) {
        try {
            boolean success = payOSPaymentService.confirmPayment(orderCode);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to confirm PayOS payment", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.ok(error);
        }
    }
}
