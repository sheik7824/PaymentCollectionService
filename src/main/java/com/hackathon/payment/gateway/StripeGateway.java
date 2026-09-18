package com.hackathon.payment.gateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Placeholder adapter. Wire the Stripe SDK / REST client here.
 */
@Component
@ConditionalOnProperty(prefix = "app.gateway", name = "provider", havingValue = "stripe")
public class StripeGateway implements PaymentGateway {

    @Override
    public String getName() {
        return "STRIPE";
    }

    @Override
    public GatewayChargeResponse charge(GatewayChargeRequest request) {
        throw new PaymentGatewayException("Stripe integration not configured");
    }
}
