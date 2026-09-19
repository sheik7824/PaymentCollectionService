package com.hackathon.payment.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateScheduledPaymentRequest(
        String orderId,
        BigDecimal amount,
        String currency,
        String paymentMethodToken,
        LocalDate nextRunDate
) {
}
