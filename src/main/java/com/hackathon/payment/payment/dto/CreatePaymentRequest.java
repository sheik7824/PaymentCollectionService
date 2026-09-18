package com.hackathon.payment.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotBlank @Size(max = 100) String orderId,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 16, fraction = 2) BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$", message = "must be a 3-letter ISO currency code") String currency,
        @Size(max = 100) String customerReference
) {
    /** Canonical string used to fingerprint the request for idempotency checks. */
    public String canonical() {
        return String.join("|", orderId, amount.stripTrailingZeros().toPlainString(), currency,
                customerReference == null ? "" : customerReference);
    }
}
