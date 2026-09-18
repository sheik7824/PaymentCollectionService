package com.hackathon.payment.gateway;

public record GatewayChargeResponse(
        GatewayStatus status,
        String gatewayReference,
        String failureReason
) {
    public static GatewayChargeResponse success(String reference) {
        return new GatewayChargeResponse(GatewayStatus.SUCCESS, reference, null);
    }

    public static GatewayChargeResponse pending(String reference) {
        return new GatewayChargeResponse(GatewayStatus.PENDING, reference, null);
    }

    public static GatewayChargeResponse failed(String reference, String reason) {
        return new GatewayChargeResponse(GatewayStatus.FAILED, reference, reason);
    }
}
