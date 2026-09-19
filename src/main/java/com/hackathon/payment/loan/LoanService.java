package com.hackathon.payment.loan;

import com.hackathon.payment.common.exception.ResourceNotFoundException;
import com.hackathon.payment.loan.dto.CreateLoanRequest;
import com.hackathon.payment.loan.dto.LoanResponse;
import com.hackathon.payment.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;

    @Transactional
    public LoanResponse apply(CreateLoanRequest request, AuthenticatedUser user) {
        Loan loan = Loan.builder()
                .id(UUID.randomUUID())
                .borrowerId(user.id())
                .principal(request.principal())
                .annualInterestRate(request.annualInterestRate())
                .termMonths(request.termMonths())
                .purpose(request.purpose())
                .status(LoanStatus.PENDING)
                .build();
        return LoanResponse.from(loanRepository.save(loan));
    }

    @Transactional(readOnly = true)
    public LoanResponse getLoan(UUID loanId, AuthenticatedUser user) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));
        return LoanResponse.from(loan);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> listLoans(AuthenticatedUser user) {
        return loanRepository.findAll().stream()
                .map(LoanResponse::from)
                .toList();
    }
}
