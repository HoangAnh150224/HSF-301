package hsf.project_gr1.controller.api;


import hsf.project_gr1.model.entity.Wallet;
import hsf.project_gr1.model.entity.Withdrawal;
import hsf.project_gr1.repository.UserRepository;
import hsf.project_gr1.security.CustomUserDetails;
import hsf.project_gr1.service.WalletService;
import hsf.project_gr1.service.WithdrawalService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Controller
@RequestMapping("api/wallet/withdraw")
public class WithdrawController {
    @Autowired
    private  WithdrawalService withdrawalService;
    @Autowired
    private  WalletService walletService;
    @Autowired
    private  UserRepository userRepository;

    @GetMapping
    public String withdrawPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Wallet wallet = walletService.getByUserId(userDetails.getId());
        model.addAttribute("balance", wallet.getBalance());
        model.addAttribute("withdrawals", withdrawalService.getWithdrawalsByUserId(userDetails.getId()));
        return "wallet/withdraw";
    }
    @GetMapping("/viewall")
    public String viewAll(Model model) {
        model.addAttribute("withdrawals", withdrawalService.getPendingWithdrawals());
        return "admin/withdrawals";
    }
    @PostMapping
    public String submitWithdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam BigDecimal amount,
            @RequestParam String bankName,
            @RequestParam String bankAccount,
            @RequestParam String accountName,
            RedirectAttributes redirectAttributes) {

        try {
            withdrawalService.requestWithdrawal(
                    userDetails.getId(), amount, bankName, bankAccount, accountName);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Gui yeu cau rut tien thanh cong! Chung toi se xu ly trong 1-3 ngay lam viec.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Withdraw error", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Co loi xay ra. Vui long thu lai.");
        }

        return "redirect:/api/wallet/withdraw";
    }

    @GetMapping("/export")
    public void exportPendingWithdrawals(HttpServletResponse response) throws IOException {
        List<Withdrawal> pendings = withdrawalService.getPendingWithdrawals();

        if (pendings.isEmpty()) {
            response.sendRedirect("/api/wallet/withdraw/viewall");
            return;
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"withdrawals_" +
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".zip\"");

        Map<String, List<Withdrawal>> groupedByBank = new HashMap<>();

        for (Withdrawal w : pendings) {
            String bankName = w.getBankName();
            if (!groupedByBank.containsKey(bankName)) {
                groupedByBank.put(bankName, new ArrayList<>());
            }
            groupedByBank.get(bankName).add(w);
        }

        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());

        for (Map.Entry<String, List<Withdrawal>> entry : groupedByBank.entrySet()) {
            String bankName = entry.getKey();
            List<Withdrawal> bankWithdrawals = entry.getValue();

            ZipEntry zipEntry = new ZipEntry(
                    "withdrawals_" + bankName + "_" +
                            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt"
            );
            zipOut.putNextEntry(zipEntry);

            PrintWriter writer = new PrintWriter(zipOut);
            writer.println("========================================");
            writer.println("  NGAN HANG: " + bankName);
            writer.println("  Ngay xuat: " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            writer.println("  Tong so yeu cau: " + bankWithdrawals.size());
            writer.println("========================================");
            writer.println();

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (int i = 0; i < bankWithdrawals.size(); i++) {
                Withdrawal w = bankWithdrawals.get(i);
                totalAmount = totalAmount.add(w.getAmount());

                writer.println("----------------------------------------");
                writer.printf("  STT          : %d%n", i + 1);
                writer.printf("  Ma GD        : #%d%n", w.getId());
                writer.printf("  Nguoi dung   : %s%n", w.getUser().getUsername());
                writer.printf("  So tien      : %,.0f VND%n", w.getAmount());
                writer.printf("  So tai khoan : %s%n", w.getBankAccount());
                writer.printf("  Ten chu TK   : %s%n", w.getAccountName());
                writer.printf("  Thoi gian    : %s%n",
                        w.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

                withdrawalService.export(w.getId());
            }

            writer.println("----------------------------------------");
            writer.println();
            writer.println("========================================");
            writer.printf("  TONG TIEN    : %,.0f VND%n", totalAmount);
            writer.println("========================================");

            writer.flush();
            zipOut.closeEntry();
        }

        zipOut.finish();
        zipOut.flush();
    }
}