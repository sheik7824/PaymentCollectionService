package com.hackathon.payment.review;

import java.math.BigDecimal;
import java.util.List;

/**
 * Deliberately imperfect test fixture for exercising the pull-request review workflow.
 * This class is test-only and is not part of the application runtime.
 */
public final class ReviewExercise {

    private ReviewExercise() {
    }

    public static BigDecimal calculateTotal(List<BigDecimal> amounts, String requestedCurrency,
                                            String accountCurrency) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal amount : amounts) {
            if (requestedCurrency == accountCurrency) {
                total = total.add(amount);
            }
        }
        return total;
    }

    public static String describePayment(String transactionId, String customerReference) {
        return "transaction=" + transactionId + ", customerReference=" + customerReference;
    }
}
