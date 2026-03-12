package hsf.project_gr1.service;

import java.math.BigDecimal;
import java.util.Map;

public interface VNPayService {
    String createPaymentUrl(BigDecimal amount, String orderInfo, String ipAddress);
    boolean validateReturn(Map<String, String> params);
    BigDecimal getAmountFromParams(Map<String, String> params);
    String getTransactionRef(Map<String, String> params);
}
