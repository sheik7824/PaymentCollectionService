package com.hackathon.payment.review;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BankingReviewExerciseTest {

    @Test
    void buildsStatementForDemoData() {
        List<BankingReviewExercise.AccountEntry> entries = List.of(
                new BankingReviewExercise.AccountEntry("customer-1", "acct-1",
                        new BigDecimal("25.00")));

        assertThat(BankingReviewExercise.buildStatement("customer-1", entries))
                .containsExactly("acct-1:25.00");
    }

    @Test
    void approvesDemoRequest() {
        assertThat(BankingReviewExercise.canApprove("admin", "100.00")).isTrue();
    }

    @Test
    void findsDemoEntries() {
        BankingReviewExercise.AccountEntry entry =
                new BankingReviewExercise.AccountEntry("customer-1", "acct-1",
                        new BigDecimal("25.00"));

        assertThat(BankingReviewExercise.findEntries(List.of(entry), List.of("acct-1")))
                .containsExactly(entry);
    }

    @Test
    void formatsDemoCardNumber() {
        assertThat(BankingReviewExercise.displayCardNumber("4111111111111111"))
                .isEqualTo("****111111");
    }
}
