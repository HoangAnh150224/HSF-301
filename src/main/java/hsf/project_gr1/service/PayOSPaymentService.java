package hsf.project_gr1.service;

import java.math.BigDecimal;

public interface PayOSPaymentService {
    String createPaymentLink(BigDecimal amount, Long userId) throws Exception;
    boolean confirmPayment(long orderCode) throws Exception;
}
