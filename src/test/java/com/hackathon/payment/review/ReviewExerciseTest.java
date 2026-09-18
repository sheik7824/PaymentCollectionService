package com.hackathon.payment.review;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewExerciseTest {

    @Test
    void calculatesTotalForMatchingCurrency() {
        BigDecimal total = ReviewExercise.calculateTotal(
                List.of(new BigDecimal("10.00"), new BigDecimal("5.50")),
                "USD",
                "USD");

        assertThat(total).isEqualByComparingTo("15.50");
    }

    @Test
    void describesPayment() {
        assertThat(ReviewExercise.describePayment("txn-1", "customer-1"))
                .contains("txn-1", "customer-1");
    }
}
