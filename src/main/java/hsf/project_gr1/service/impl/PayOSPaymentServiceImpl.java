package hsf.project_gr1.service.impl;

import hsf.project_gr1.config.PayOSConfig;
import hsf.project_gr1.model.entity.Wallet;
import hsf.project_gr1.model.entity.WalletTransaction;
import hsf.project_gr1.model.enums.WalletTransactionStatus;
import hsf.project_gr1.model.enums.WalletTransactionType;
import hsf.project_gr1.repository.WalletRepository;
import hsf.project_gr1.repository.WalletTransactionRepository;
import hsf.project_gr1.service.PayOSPaymentService;
import hsf.project_gr1.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayOSPaymentServiceImpl implements PayOSPaymentService {

    private final PayOS payOS;
    private final PayOSConfig payOSConfig;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletService walletService;

    @Override
    @Transactional
    public String createPaymentLink(BigDecimal amount, Long userId) throws Exception {
        long orderCode = System.currentTimeMillis();

        // Create a PENDING wallet transaction to track this payment
        Wallet wallet = walletService.getByUserId(userId);
        WalletTransaction pendingTx = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(WalletTransactionType.DEPOSIT)
                .status(WalletTransactionStatus.PENDING)
                .transactionCode(UUID.randomUUID().toString().substring(0, 30))
                .paymentMethod("PAYOS")
                .paymentTransactionId(String.valueOf(orderCode))
                .description("Nap tien vao vi qua PayOS")
                .build();
        walletTransactionRepository.save(pendingTx);

        CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amount.longValue())
                .description("Nap vi " + userId)
                .returnUrl(payOSConfig.getReturnUrl() + "&orderCode=" + orderCode)
                .cancelUrl(payOSConfig.getCancelUrl())
                .build();

        CreatePaymentLinkResponse response = payOS.paymentRequests().create(request);
        log.info("Created PayOS payment link: orderCode={}, checkoutUrl={}", orderCode, response.getCheckoutUrl());
        return response.getCheckoutUrl();
    }

    @Override
    @Transactional
    public boolean confirmPayment(long orderCode) throws Exception {
        // Check if already processed
        Optional<WalletTransaction> existingTx = walletTransactionRepository
                .findByPaymentTransactionId(String.valueOf(orderCode));

        if (existingTx.isPresent() && existingTx.get().getStatus() == WalletTransactionStatus.COMPLETED) {
            log.info("Payment already confirmed: orderCode={}", orderCode);
            return true;
        }

        // Call PayOS API to check payment status
        PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);
        log.info("PayOS payment status: orderCode={}, status={}, amount={}", orderCode, paymentLink.getStatus(), paymentLink.getAmount());

        if (paymentLink.getStatus() == PaymentLinkStatus.PAID) {
            if (existingTx.isPresent()) {
                WalletTransaction tx = existingTx.get();
                Wallet wallet = tx.getWallet();

                // Update wallet balance
                wallet.setBalance(wallet.getBalance().add(tx.getAmount()));
                walletRepository.save(wallet);

                // Mark transaction as completed
                tx.setStatus(WalletTransactionStatus.COMPLETED);
                walletTransactionRepository.save(tx);

                log.info("Deposited {} VND to wallet of user via PayOS orderCode={}", tx.getAmount(), orderCode);
                return true;
            }
        }

        return false;
    }
}
