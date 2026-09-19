package com.hackathon.payment.payment.dto;

import com.hackathon.payment.payment.ScheduledPayment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ScheduledPaymentResponse(
        UUID id,
        String orderId,
        BigDecimal amount,
        String currency,
        String paymentMethodToken,
        String scheduleExpression,
        LocalDate nextRunDate,
        Instant createdAt
) {
    public static ScheduledPaymentResponse from(ScheduledPayment payment) {
        return new ScheduledPaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethodToken(),
                payment.getScheduleExpression(),
                payment.getNextRunDate(),
                payment.getCreatedAt());
    }
}
