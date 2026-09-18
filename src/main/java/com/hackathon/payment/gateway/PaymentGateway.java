package com.hackathon.payment.gateway;

/**
 * Adapter contract for external payment providers.
 */
public interface PaymentGateway {

    String getName();

    /**
     * @throws PaymentGatewayException on technical failure (eligible for retry)
     */
    GatewayChargeResponse charge(GatewayChargeRequest request);
}
