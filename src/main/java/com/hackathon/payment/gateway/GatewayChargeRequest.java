package com.hackathon.payment.gateway;

import java.math.BigDecimal;

public record GatewayChargeRequest(
        String transactionId,
        String orderId,
        BigDecimal amount,
        String currency,
        String customerReference
) {
}
