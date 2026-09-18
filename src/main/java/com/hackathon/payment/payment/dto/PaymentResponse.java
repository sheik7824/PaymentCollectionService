package com.hackathon.payment.payment.dto;

import com.hackathon.payment.payment.Transaction;
import com.hackathon.payment.payment.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String transactionId,
        String orderId,
        BigDecimal amount,
        String currency,
        String customerReference,
        TransactionStatus status,
        String gatewayName,
        String gatewayReference,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static PaymentResponse from(Transaction t) {
        return new PaymentResponse(
                t.getTransactionId(),
                t.getOrderId(),
                t.getAmount(),
                t.getCurrency(),
                t.getCustomerReference(),
                t.getStatus(),
                t.getGatewayName(),
                t.getGatewayReference(),
                t.getFailureReason(),
                t.getCreatedAt(),
                t.getUpdatedAt());
    }
}
