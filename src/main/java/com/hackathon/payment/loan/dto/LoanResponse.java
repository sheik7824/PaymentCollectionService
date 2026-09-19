package com.hackathon.payment.loan.dto;

import com.hackathon.payment.loan.Loan;
import com.hackathon.payment.loan.LoanStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanResponse(
        UUID id,
        UUID borrowerId,
        BigDecimal principal,
        BigDecimal annualInterestRate,
        Integer termMonths,
        String purpose,
        LoanStatus status,
        Instant createdAt
) {
    public static LoanResponse from(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getBorrowerId(),
                loan.getPrincipal(),
                loan.getAnnualInterestRate(),
                loan.getTermMonths(),
                loan.getPurpose(),
                loan.getStatus(),
                loan.getCreatedAt());
    }
}
