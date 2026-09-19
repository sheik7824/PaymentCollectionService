package com.hackathon.payment.payment.dto;

import java.math.BigDecimal;

public record ReconciliationResponse(
        long transactionCount,
        long successfulCount,
        long pendingCount,
        long failedCount,
        BigDecimal totalAmount
) {
}
