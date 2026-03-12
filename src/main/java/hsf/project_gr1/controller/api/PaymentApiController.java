package hsf.project_gr1.controller.api;

import hsf.project_gr1.service.PayOSPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentApiController {

    private final PayOS payOS;
    private final PayOSPaymentService payOSPaymentService;

    @PostMapping("/payos-hook")
    public ResponseEntity<Map<String, String>> handlePayOSWebhook(@RequestBody Webhook webhook) {
        try {
            WebhookData webhookData = payOS.webhooks().verify(webhook);
            log.info("PayOS webhook received: orderCode={}, code={}", webhookData.getOrderCode(), webhookData.getCode());

            if ("00".equals(webhookData.getCode())) {
                long orderCode = webhookData.getOrderCode();
                payOSPaymentService.confirmPayment(orderCode);
            }

            return ResponseEntity.ok(Map.of("success", "true"));
        } catch (Exception e) {
            log.error("PayOS webhook processing failed", e);
            return ResponseEntity.ok(Map.of("success", "false"));
        }
    }
}
