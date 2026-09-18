package com.hackathon.payment.gateway;

/**
 * Transient/technical gateway failure (network, 5xx). Retried by the caller.
 * Business declines are NOT exceptions; they are returned as {@link GatewayStatus#FAILED}.
 */
public class PaymentGatewayException extends RuntimeException {

    public PaymentGatewayException(String message) {
        super(message);
    }

    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
