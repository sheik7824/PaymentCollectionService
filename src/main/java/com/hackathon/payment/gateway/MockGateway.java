package com.hackathon.payment.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Deterministic mock: amount ending in .99 fails, amount ending in .50 stays pending,
 * anything else succeeds. Amount exactly 999 simulates a gateway outage.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.gateway", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockGateway implements PaymentGateway {

    @Override
    public String getName() {
        return "MOCK";
    }

    @Override
    public GatewayChargeResponse charge(GatewayChargeRequest request) {
        log.info("MockGateway charging txn={} amount={} {}", request.transactionId(), request.amount(),
                request.currency());

        if (request.amount().compareTo(new BigDecimal("999")) == 0) {
            throw new PaymentGatewayException("Simulated gateway outage");
        }

        String reference = "PG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String cents = request.amount().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
        if (cents.endsWith(".99")) {
            return GatewayChargeResponse.failed(reference, "Insufficient funds");
        }
        if (cents.endsWith(".50")) {
            return GatewayChargeResponse.pending(reference);
        }
        return GatewayChargeResponse.success(reference);
    }
}
