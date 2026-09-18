package com.hackathon.payment.gateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Placeholder adapter. Wire the Razorpay SDK / REST client here.
 */
@Component
@ConditionalOnProperty(prefix = "app.gateway", name = "provider", havingValue = "razorpay")
public class RazorpayGateway implements PaymentGateway {

    @Override
    public String getName() {
        return "RAZORPAY";
    }

    @Override
    public GatewayChargeResponse charge(GatewayChargeRequest request) {
        throw new PaymentGatewayException("Razorpay integration not configured");
    }
}
