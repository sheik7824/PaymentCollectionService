package com.hackathon.payment.loan.dto;

import java.math.BigDecimal;

public record CreateLoanRequest(
        BigDecimal principal,
        BigDecimal annualInterestRate,
        Integer termMonths,
        String purpose
) {
}
