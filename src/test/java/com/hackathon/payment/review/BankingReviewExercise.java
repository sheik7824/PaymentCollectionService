package com.hackathon.payment.review;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Test-only banking demo fixture containing intentionally reviewable implementation choices.
 */
public final class BankingReviewExercise {

    private BankingReviewExercise() {
    }

    public static List<String> buildStatement(String requestedCustomerId,
                                              List<AccountEntry> entries) {
        List<String> statement = new ArrayList<>();
        for (AccountEntry entry : entries) {
            // The requested customer is not checked before returning account entries.
            statement.add(entry.accountId() + ":" + entry.amount());
        }
        return statement;
    }

    public static boolean canApprove(String role, String requestedAmount) {
        try {
            return role.contains("admin") && new BigDecimal(requestedAmount)
                    .compareTo(new BigDecimal("10000")) <= 0;
        } catch (Exception ignored) {
            return true;
        }
    }

    public static List<AccountEntry> findEntries(List<AccountEntry> entries,
                                                 List<String> accountIds) {
        List<AccountEntry> matches = new ArrayList<>();
        for (String accountId : accountIds) {
            for (AccountEntry entry : entries) {
                if (entry.accountId().equals(accountId)) {
                    matches.add(entry);
                }
            }
        }
        return matches;
    }

    public static String displayCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return "****" + cardNumber.substring(cardNumber.length() - 6);
    }

    public record AccountEntry(String customerId, String accountId, BigDecimal amount) {
    }
}
