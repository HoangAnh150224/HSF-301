package hsf.project_gr1.controller.api;

import hsf.project_gr1.model.entity.Dispute;
import hsf.project_gr1.model.enums.DisputeStatus;
import hsf.project_gr1.security.CustomUserDetails;
import hsf.project_gr1.service.DisputeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
public class DisputeApiController {

    private final DisputeService disputeService;

    @PostMapping("/create")
    public ResponseEntity<Dispute> createDispute(
            @RequestParam Long transactionId,
            @RequestParam String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long creatorId = userDetails.getId();
        return ResponseEntity.ok(disputeService.createDispute(transactionId, creatorId, reason));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<Dispute> resolveDispute(@PathVariable Long id, @RequestParam DisputeStatus resolution, @RequestParam String comment) {
        return ResponseEntity.ok(disputeService.resolveDispute(id, resolution, comment));
    }

    @GetMapping("/list")
    public ResponseEntity<java.util.List<Dispute>> getAllDisputes() {
        return ResponseEntity.ok(disputeService.getAllDisputes());
    }
}
